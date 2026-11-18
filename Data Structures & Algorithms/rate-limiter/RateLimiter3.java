import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
/**
 * STAGE 3: make each bucket thread-safe with `synchronized`.
 *
 * Why it's needed: tryAcquire() is "read tokens, check, subtract" = 3 steps.
 * Two threads can both read tokens=1, both pass the check, both subtract -> 2 requests
 * allowed for 1 token. (A check-then-act race.)
 *
 * Why per-bucket and not one global lock: different clients never block each other.
 */

public class RateLimiter3 {
    double capacity; // the maximum number of tokens in the bucket
    double refillRate; // tokens per second
    LongSupplier nano; // clock source for time in nanoseconds
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    RateLimiter3(double capacity, double refillRate, LongSupplier nano) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.nano = nano;
    }

    public boolean tryAcquire(String clientId) {
        return buckets.computeIfAbsent(clientId, id -> new Bucket()).tryAcquire();
    }

    private final class Bucket {
        double tokens;
        long lastRefillTime;

        Bucket() {
            tokens = capacity;
            lastRefillTime = nano.getAsLong();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        void refill() {
            long now = nano.getAsLong();
            double tokensToBeAdd = (now - lastRefillTime)/1e9 * refillRate;
            tokens = Math.min(capacity, tokens + tokensToBeAdd);
            lastRefillTime = now;
        }
    }

    public static void check(String name, Object actual, Object expected) {
        boolean ok = java.util.Objects.equals(actual, expected);
        System.out.printf("%-40s expected=%-6s actual=%-6s %s%n", name, expected, actual, ok ? "PASS" : "FAIL");
        if (!ok) throw new AssertionError(name);
    }

    public static void main() throws InterruptedException {
        AtomicLong now = new AtomicLong(0);
        RateLimiter3 rateLimiter3 = new RateLimiter3(2, 1, now::get);

        check("client1 #1", rateLimiter3.tryAcquire("client1"), true);
        check("client1 #2", rateLimiter3.tryAcquire("client1"), true);
        check("client1 #3", rateLimiter3.tryAcquire("client1"), false);
        check("client2 is independent", rateLimiter3.tryAcquire("client2"), true);

        // Concurrency test: capacity 1000, NO refill, 16 threads x 200 = 3200 requests.
        // Exactly 1000 must be allowed, never more.

        RateLimiter3 rateLimiter3b = new RateLimiter3(1000, 0, now::get);
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch latch = new CountDownLatch(16);
        AtomicInteger allowed = new AtomicInteger(0);

        for (int i = 0; i < 16; i++) { 
            executor.submit( () -> {
                for(int j=0;j<200;j++) {
                    if (rateLimiter3b.tryAcquire("client1")) {
                        allowed.incrementAndGet();
                    }
                    
                }
                latch.countDown();
            });
            
    }
    latch.await();
    executor.shutdown();
    check("concurrency test: exactly 1000 allowed", allowed.get(), 1000);
}}
