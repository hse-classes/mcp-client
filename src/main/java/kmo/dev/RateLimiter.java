package kmo.dev;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Tiny sliding-window limiter: at most {@code limit} hits per key within {@code window}. */
public class RateLimiter {

    private static final int SWEEP_EVERY = 1000;

    private final int limit;
    private final long windowNanos;
    private final ConcurrentMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();

    public RateLimiter(int limit, Duration window) {
        this.limit = limit;
        this.windowNanos = window.toNanos();
    }

    public boolean tryAcquire(String key) {
        return tryAcquire(key, System.nanoTime());
    }

    boolean tryAcquire(String key, long nowNanos) {
        if (calls.incrementAndGet() % SWEEP_EVERY == 0) {
            sweep(nowNanos);
        }
        // compute() is atomic per key, so a concurrent sweep can never leave two
        // requests of the same client working on different buckets.
        boolean[] allowed = new boolean[1];
        hits.compute(key, (k, times) -> {
            if (times == null) {
                times = new ArrayDeque<>();
            }
            prune(times, nowNanos);
            if (times.size() < limit) {
                times.addLast(nowNanos);
                allowed[0] = true;
            }
            return times;
        });
        return allowed[0];
    }

    /** Drops the buckets of clients that have not been seen for a whole window. */
    private void sweep(long nowNanos) {
        for (String key : hits.keySet()) {
            hits.computeIfPresent(key, (k, times) -> {
                prune(times, nowNanos);
                return times.isEmpty() ? null : times;
            });
        }
    }

    private void prune(Deque<Long> times, long nowNanos) {
        while (!times.isEmpty() && nowNanos - times.peekFirst() > windowNanos) {
            times.pollFirst();
        }
    }

    int size() {
        return hits.size();
    }
}
