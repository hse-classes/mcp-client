package kmo.dev;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void sweepRemovesIdleClients() {
        var limiter = new RateLimiter(1, Duration.ofSeconds(1));
        for (int i = 0; i < 999; i++) {
            limiter.tryAcquire("ip-" + i, 0);
        }
        // The 1000th call triggers a sweep far past everyone's window.
        limiter.tryAcquire("late", Duration.ofMinutes(1).toNanos());
        assertEquals(1, limiter.size());
    }
}
