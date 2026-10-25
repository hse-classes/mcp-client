package kmo.dev;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class RateLimiterTest {

    @Test
    void blocksAboveLimitAndRecoversAfterWindow() {
        var limiter = new RateLimiter(2, Duration.ofSeconds(10));
        long t0 = 0;
        assertTrue(limiter.tryAcquire("a", t0));
        assertTrue(limiter.tryAcquire("a", t0 + 1));
        assertFalse(limiter.tryAcquire("a", t0 + 2));
        assertTrue(limiter.tryAcquire("b", t0 + 2));
        assertTrue(limiter.tryAcquire("a", Duration.ofSeconds(11).toNanos()));
    }
}
