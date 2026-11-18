import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;


/**
 * STAGE 1: the token bucket idea, for ONE client, on ONE thread.
 *
 * Picture a bucket that holds up to `capacity` tokens. Every request must take 1 token.
 * Tokens drip back in at `refillPerSec`. Empty bucket => request rejected.
 *
 * Key trick: we don't run a timer that adds tokens. Instead, when a request arrives we
 * compute how many tokens WOULD have dripped in since last time ("lazy refill").
 */

public class RateLimiter1 {
    private double capacity; // the maximum number of tokens in the bucket
    private double refillRate; // tokens per second
    private LongSupplier nano; // clock source for time in nanoseconds

    private double tokens; // current number of tokens
    private long lastRefillTime; // last time tokens were refilled

    RateLimiter1(double capacity, double refillRate, LongSupplier nano) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.nano = nano;

        this.tokens = capacity;
        this.lastRefillTime = nano.getAsLong();
    }

    public boolean tryAcquire() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    public void refill() {
        long now = nano.getAsLong();
        double tokensToBeAdd = (now - lastRefillTime)/1e9 * refillRate;
        tokens = Math.min(capacity, tokens + tokensToBeAdd);
        lastRefillTime = now;
    }

    static void check(String name, Object actual, Object expected) {
        boolean ok = java.util.Objects.equals(actual, expected);
        System.out.printf("%-40s expected=%-6s actual=%-6s %s%n", name, expected, actual, ok ? "PASS" : "FAIL");
        if (!ok)
            throw new AssertionError(name);
    }

    public static void main() {
        AtomicLong clock = new AtomicLong(0);
        RateLimiter1 rateLimiter1 = new RateLimiter1(2, 1, clock::get);

        check("request 1", rateLimiter1.tryAcquire(), true);
        check("request 2", rateLimiter1.tryAcquire(), true);
        check("request 3 (bucket empty)", rateLimiter1.tryAcquire(), false);

        clock.addAndGet(1_000_000_000L);                                      // +1 second
        check("after 1s: 1 token refilled", rateLimiter1.tryAcquire(), true);
        check("immediately again", rateLimiter1.tryAcquire(), false);

        clock.addAndGet(10_000_000_000L);                                     // +10 seconds
        check("after 10s: capped at capacity (1)", rateLimiter1.tryAcquire(), true);
        check("capped at capacity (2)", rateLimiter1.tryAcquire(), true);
        check("capped at capacity (3: none left)", rateLimiter1.tryAcquire(), false);
    }
}
