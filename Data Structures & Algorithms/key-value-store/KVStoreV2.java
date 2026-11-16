import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/**
 * Thread-safe in-memory KV store: ConcurrentHashMap + PriorityQueue (TTL) + per-thread txn stack.
 *
 * - data:       ConcurrentHashMap, so get/put/delete are lock-free for readers and per-key atomic.
 * - TTL heap:   PriorityQueue ordered by expiry time (min-heap), guarded by its own lock.
 *               Overwrites/deletes do NOT remove old heap nodes (that would be O(n)). Instead,
 *               each node remembers the exact Entry it was created for; when it comes due we
 *               call data.remove(key, entry), which only removes if that same Entry is still
 *               current. Stale nodes are therefore harmless no-ops.
 * - Txn stack:  ThreadLocal stack of overlay maps (tombstone = delete). commit() of the outermost
 *               transaction applies the overlay under commitLock so two multi-key commits never
 *               interleave key by key.
 */
public class KVStoreV2 implements AutoCloseable {

    private static final long NO_EXPIRY = Long.MAX_VALUE;

    /** Immutable. No equals() override on purpose: identity is used by data.remove(key, entry). */
    private static final class Entry {
        final String value;      // null for tombstone
        final long expiresAt;    // absolute millis

        Entry(String value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }

    private static final Entry TOMBSTONE = new Entry(null, NO_EXPIRY);

    private static final class ExpiryNode {
        final String key;
        final Entry entry;

        ExpiryNode(String key, Entry entry) {
            this.key = key;
            this.entry = entry;
        }
    }

    private final ConcurrentHashMap<String, Entry> data = new ConcurrentHashMap<>();

    private final PriorityQueue<ExpiryNode> expiryHeap =
            new PriorityQueue<>((a, b) -> Long.compare(a.entry.expiresAt, b.entry.expiresAt));
    private final Lock heapLock = new ReentrantLock();

    private final Lock commitLock = new ReentrantLock();

    // Head of deque = innermost transaction.
    private final ThreadLocal<Deque<Map<String, Entry>>> txStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    private final LongSupplier clock;
    private final ScheduledExecutorService sweeper;

    public KVStoreV2() {
        this(System::currentTimeMillis, 100);
    }

    /** sweepIntervalMillis <= 0 disables the background sweeper (tests call sweepExpired()). */
    public KVStoreV2(LongSupplier clock, long sweepIntervalMillis) {
        this.clock = clock;
        if (sweepIntervalMillis > 0) {
            sweeper = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "kv-ttl-sweeper");
                t.setDaemon(true);
                return t;
            });
            sweeper.scheduleWithFixedDelay(this::sweepExpired,
                    sweepIntervalMillis, sweepIntervalMillis, TimeUnit.MILLISECONDS);
        } else {
            sweeper = null;
        }
    }

    // ---------------------------------------------------------------- public API

    public void put(String key, String value) {
        write(key, new Entry(value, NO_EXPIRY));
    }

    /** ttlMillis <= 0 means no expiry. */
    public void put(String key, String value, long ttlMillis) {
        long expiresAt = ttlMillis > 0 ? clock.getAsLong() + ttlMillis : NO_EXPIRY;
        write(key, new Entry(value, expiresAt));
    }

    public String get(String key) {
        long now = clock.getAsLong();

        for (Map<String, Entry> overlay : txStack.get()) {   // innermost first
            Entry e = overlay.get(key);
            if (e != null) {
                return (e == TOMBSTONE || e.isExpired(now)) ? null : e.value;
            }
        }

        Entry e = data.get(key);
        if (e == null) {
            return null;
        }
        if (e.isExpired(now)) {
            data.remove(key, e);   // lazy cleanup; no-op if someone already replaced it
            return null;
        }
        return e.value;
    }

    public void delete(String key) {
        write(key, TOMBSTONE);
    }

    public void begin() {
        txStack.get().push(new HashMap<>());
    }

    public void commit() {
        Deque<Map<String, Entry>> stack = txStack.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("No active transaction");
        }
        Map<String, Entry> overlay = stack.pop();

        if (!stack.isEmpty()) {
            stack.peek().putAll(overlay);   // nested: merge into parent only
            return;
        }

        commitLock.lock();
        try {
            for (Map.Entry<String, Entry> me : overlay.entrySet()) {
                applyToStore(me.getKey(), me.getValue());
            }
        } finally {
            commitLock.unlock();
        }
    }

    public void rollback() {
        Deque<Map<String, Entry>> stack = txStack.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("No active transaction");
        }
        stack.pop();
    }

    @Override
    public void close() {
        if (sweeper != null) {
            sweeper.shutdownNow();
        }
    }

    // ------------------------------------------------------------------ internals

    private void write(String key, Entry entry) {
        Deque<Map<String, Entry>> stack = txStack.get();
        if (!stack.isEmpty()) {
            stack.peek().put(key, entry);
        } else {
            applyToStore(key, entry);   // single-key write: CHM makes it atomic
        }
    }

    private void applyToStore(String key, Entry entry) {
        if (entry == TOMBSTONE) {
            data.remove(key);
            return;
        }
        data.put(key, entry);
        if (entry.expiresAt != NO_EXPIRY) {
            heapLock.lock();
            try {
                expiryHeap.add(new ExpiryNode(key, entry));
            } finally {
                heapLock.unlock();
            }
        }
    }

    /** Pops every due node (O(k log n) for k expired) and removes the ones still current. */
    void sweepExpired() {
        long now = clock.getAsLong();
        List<ExpiryNode> due = new ArrayList<>();

        heapLock.lock();
        try {
            ExpiryNode n;
            while ((n = expiryHeap.peek()) != null && n.entry.isExpired(now)) {
                due.add(expiryHeap.poll());
            }
        } finally {
            heapLock.unlock();
        }

        for (ExpiryNode n : due) {
            data.remove(n.key, n.entry);   // only if this exact Entry is still the current one
        }
    }

    // visible for tests
    int heapSize() {
        heapLock.lock();
        try {
            return expiryHeap.size();
        } finally {
            heapLock.unlock();
        }
    }

    int storeSize() {
        return data.size();
    }

    // ------------------------------------------------------------------ demo/tests

    private static void check(String name, Object actual, Object expected) {
        boolean ok = (actual == null) ? expected == null : actual.equals(expected);
        System.out.printf("%-52s expected=%-6s actual=%-6s %s%n",
                name, expected, actual, ok ? "PASS" : "FAIL");
        if (!ok) throw new AssertionError(name);
    }

    public static void main(String[] args) throws Exception {
        AtomicLong time = new AtomicLong(0);
        try (KVStoreV2 store = new KVStoreV2(time::get, 0)) {

            // Example from the question
            store.put("a", "10");
            store.begin();
            store.put("a", "20");
            check("get(a) inside txn", store.get("a"), "20");
            store.rollback();
            check("get(a) after rollback", store.get("a"), "10");

            store.begin();
            store.put("a", "30");
            store.commit();
            check("get(a) after commit", store.get("a"), "30");

            store.begin();
            store.delete("a");
            check("get(a) deleted in txn", store.get("a"), null);
            store.rollback();
            check("get(a) after rollback of delete", store.get("a"), "30");

            // Nested
            store.begin();
            store.put("b", "1");
            store.begin();
            store.put("b", "2");
            store.rollback();
            check("inner rollback keeps outer value", store.get("b"), "1");
            store.begin();
            store.put("b", "3");
            store.commit();
            store.commit();
            check("nested commit then outer commit", store.get("b"), "3");

            // TTL via heap
            store.put("t", "x", 100);                       // expires at t=100
            time.set(99);
            check("TTL not yet expired", store.get("t"), "x");
            time.set(100);
            check("TTL expired (lazy)", store.get("t"), null);

            // Sweeper removes physically
            time.set(1000);
            store.put("s", "v", 50);                        // expires at 1050
            int before = store.storeSize();
            time.set(1050);
            store.sweepExpired();
            check("sweeper removed expired key", store.storeSize(), before - 1);
            check("heap drained after sweep", store.heapSize(), 0);

            // Stale heap node must NOT delete a newer value
            time.set(2000);
            store.put("k", "ttl", 100);                     // heap node expires at 2100
            store.put("k", "forever");                      // overwrite, no TTL
            time.set(2100);
            store.sweepExpired();
            check("overwrite w/o TTL survives stale heap node", store.get("k"), "forever");

            // Overwrite TTL with a longer TTL
            time.set(3000);
            store.put("m", "v1", 100);                      // old node @3100
            store.put("m", "v2", 500);                      // new node @3500
            time.set(3100);
            store.sweepExpired();
            check("longer TTL survives old node", store.get("m"), "v2");
            time.set(3500);
            check("longer TTL expires later", store.get("m"), null);

            // Delete then re-put with the same key
            time.set(4000);
            store.put("d", "old", 100);
            store.delete("d");
            store.put("d", "new");
            time.set(4100);
            store.sweepExpired();
            check("delete + re-put survives stale node", store.get("d"), "new");

            // TTL set inside a transaction
            time.set(5000);
            store.begin();
            store.put("tx", "v", 100);
            store.commit();
            time.set(5100);
            check("TTL set in txn expires after commit", store.get("tx"), null);

            // Isolation
            store.put("c", "old");
            store.begin();
            store.put("c", "new");
            String[] seen = new String[1];
            Thread other = new Thread(() -> seen[0] = store.get("c"));
            other.start();
            other.join();
            check("other thread can't see uncommitted", seen[0], "old");
            store.commit();
            check("committed value visible", store.get("c"), "new");

            // Concurrency: commits of {x,y} must never interleave (x == y at the end)
            ExecutorService pool = Executors.newFixedThreadPool(8);
            CountDownLatch done = new CountDownLatch(8);
            for (int t = 0; t < 8; t++) {
                final String tag = "T" + t;
                pool.submit(() -> {
                    for (int i = 0; i < 2000; i++) {
                        store.begin();
                        store.put("x", tag);
                        store.put("y", tag);
                        store.commit();
                    }
                    done.countDown();
                });
            }
            done.await();
            pool.shutdown();
            check("multi-key commits never interleave", store.get("x"), store.get("y"));
        }
        System.out.println("All tests passed");
    }
}