package dev.porama.gradingcore.core.messenger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RequeueLimiter {
    private Map<Integer, AtomicInteger> entries = new HashMap<>();

    public synchronized void increment(int id) {
        entries.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
    }

    public synchronized int getCount(int id) {
        if (!entries.containsKey(id)) return 0;
        return entries.get(id).get();
    }

    public synchronized void reset(int id) {
        entries.remove(id);
    }

    public synchronized void resetAll() {
        entries.clear();
    }

    public boolean hasExceeded(int submissionId, int maxRequeue) {
        return getCount(submissionId) > maxRequeue;
    }

    public static class Entry {
        private final int id;
        private final AtomicInteger requeueCount = new AtomicInteger(0);

        public Entry(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public AtomicInteger getRequeueCount() {
            return requeueCount;
        }
    }
}
