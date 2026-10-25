package kmo.dev;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Tiny sliding-window limiter: at most {@code limit} hits per key within {@code window}. */
public class RateLimiter {

    private final int limit;
    private final long windowNanos;
    private final ConcurrentMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public RateLimiter(int limit, Duration window) {
        this.limit = limit;
        this.windowNanos = window.toNanos();
    }

    public boolean tryAcquire(String key) {
        return tryAcquire(key, System.nanoTime());
    }

    boolean tryAcquire(String key, long nowNanos) {
        Deque<Long> times = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && nowNanos - times.peekFirst() > windowNanos) {
                times.pollFirst();
            }
            if (times.size() >= limit) {
                return false;
            }
            times.addLast(nowNanos);
            return true;
        }
    }
}
