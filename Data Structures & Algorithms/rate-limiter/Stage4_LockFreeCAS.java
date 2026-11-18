import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * STAGE 4: lock-free using compare-and-set (CAS).
 *
 * Idea: keep the bucket's whole state in ONE immutable object (tokens + lastRefill).
 *   loop:  read the current state -> compute the new state -> CAS(old, new)
 *   CAS succeeds only if nobody changed the state in between; otherwise retry.
 * No thread ever blocks while holding a lock, so a slow thread can't stall others.
 *
 * Bonus: a DENIED request doesn't need to write anything, so it skips the CAS.
 */
public class Stage4_LockFreeCAS {

    private final double capacity;
    private final double refillPerSec;
    private final LongSupplier nanoClock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Stage4_LockFreeCAS(double capacity, double refillPerSec, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.nanoClock = nanoClock;
    }

    public boolean allowRequest(String clientId) {
        return buckets.computeIfAbsent(clientId, id -> new Bucket()).tryAcquire();
    }

    /** Immutable snapshot. Safe to share; "changing" it means creating a new one. */
    private static final class State {
        final double tokens;
        final long lastRefillNanos;
        State(double tokens, long lastRefillNanos) { this.tokens = tokens; this.lastRefillNanos = lastRefillNanos; }
    }

    private final class Bucket {
        private final AtomicReference<State> state =
                new AtomicReference<>(new State(capacity, nanoClock.getAsLong()));

        boolean tryAcquire() {
            while (true) {
                State old = state.get();
                long now = nanoClock.getAsLong();
                long elapsed = Math.max(0, now - old.lastRefillNanos);
                double tokens = Math.min(capacity, old.tokens + elapsed / 1e9 * refillPerSec);

                if (tokens < 1) return false;                         // denied: nothing to write

                State next = new State(tokens - 1, Math.max(now, old.lastRefillNanos));
                if (state.compareAndSet(old, next)) return true;      // won the race
                // else: another thread changed the state first -> loop and try again
            }
        }
    }

    static void check(String name, Object actual, Object expected) {
        boolean ok = java.util.Objects.equals(actual, expected);
        System.out.printf("%-40s expected=%-6s actual=%-6s %s%n", name, expected, actual, ok ? "PASS" : "FAIL");
        if (!ok) throw new AssertionError(name);
    }

    public static void main(String[] args) throws Exception {
        AtomicLong now = new AtomicLong(0);
        Stage4_LockFreeCAS rl = new Stage4_LockFreeCAS(2, 1, now::get);
        check("client1 #1", rl.allowRequest("client1"), true);
        check("client1 #2", rl.allowRequest("client1"), true);
        check("client1 #3", rl.allowRequest("client1"), false);
        now.addAndGet(1_000_000_000L);
        check("after 1s", rl.allowRequest("client1"), true);

        Stage4_LockFreeCAS shared = new Stage4_LockFreeCAS(1000, 0, System::nanoTime);
        AtomicInteger allowed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch done = new CountDownLatch(16);
        for (int t = 0; t < 16; t++) {
            pool.submit(() -> {
                for (int i = 0; i < 200; i++) if (shared.allowRequest("c")) allowed.incrementAndGet();
                done.countDown();
            });
        }
        done.await();
        pool.shutdown();
        check("3200 racing requests, 1000 tokens", allowed.get(), 1000);
    }
}