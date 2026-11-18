import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * STAGE 2: one bucket PER CLIENT.
 * New idea: ConcurrentHashMap<clientId, Bucket>, buckets created lazily with computeIfAbsent.
 * This is the shape of the API from the question: allowRequest(clientId).
 *
 * Limitation (fixed in stage 3): the Bucket itself is still not thread-safe.
 */

public class RateLimiter2 {
    double capacity; // the maximum number of tokens in the bucket
    double refillRate; // tokens per second
    LongSupplier nano; // clock source for time in nanoseconds
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    RateLimiter2(double capacity, double refillRate, LongSupplier nano) {
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

        boolean tryAcquire() {
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

    public static void main() {
        AtomicLong now = new AtomicLong(0);
        RateLimiter2 rateLimiter2 = new RateLimiter2(2, 1, now::get);

        check("client1 #1", rateLimiter2.tryAcquire("client1"), true);
        check("client1 #2", rateLimiter2.tryAcquire("client1"), true);
        check("client1 #3", rateLimiter2.tryAcquire("client1"), false);
        check("client2 is independent", rateLimiter2.tryAcquire("client2"), true);
    }
}
