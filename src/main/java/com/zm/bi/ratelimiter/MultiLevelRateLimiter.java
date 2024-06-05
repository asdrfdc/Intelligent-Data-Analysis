package com.zm.bi.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiLevelRateLimiter {
    private static final long PERMITS_PER_MINUTE = 100;
    private static final long PERMITS_PER_HOUR = 300 * 60; // 300 requests per hour
    private static final long PERMITS_PER_DAY = 1000 * 60 * 60; // 1000 requests per day

    private final TreeMap<Instant, Integer> minuteRequestCountMap = new TreeMap<>();
    private final TreeMap<Instant, Integer> hourRequestCountMap = new TreeMap<>();
    private final TreeMap<Instant, Integer> dayRequestCountMap = new TreeMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MultiLevelRateLimiter() {
        scheduler.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.MINUTES);
    }

    public synchronized boolean tryAcquire() {
        Instant now = Instant.now();

        // 检查每分钟限制
        if (!tryAcquirePerWindow(minuteRequestCountMap, now, Duration.ofMinutes(1), PERMITS_PER_MINUTE)) {
            return false;
        }

        // 检查每小时限制
        if (!tryAcquirePerWindow(hourRequestCountMap, now, Duration.ofHours(1), PERMITS_PER_HOUR)) {
            return false;
        }

        // 检查每日限制
        if (!tryAcquirePerWindow(dayRequestCountMap, now, Duration.ofDays(1), PERMITS_PER_DAY)) {
            return false;
        }

        return true;
    }

    private boolean tryAcquirePerWindow(TreeMap<Instant, Integer> countMap, Instant now, Duration windowSize, long limit) {
        Instant windowStart = now.minus(windowSize);
        int currentWindowCount = getRequestsInCurrentWindow(countMap, windowStart);
        if (currentWindowCount >= limit) {
            return false;
        }
        countMap.merge(now, 1, Integer::sum);
        return true;
    }

    private int getRequestsInCurrentWindow(TreeMap<Instant, Integer> countMap, Instant windowStart) {
        return countMap.tailMap(windowStart).values().stream().mapToInt(Integer::intValue).sum();
    }

    private void cleanExpiredEntries(TreeMap<Instant, Integer> countMap, Duration windowSize) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowSize);
        countMap.headMap(windowStart).clear(); // 清除已过期的条目
    }

    private void cleanUp() {
        cleanExpiredEntries(minuteRequestCountMap, Duration.ofMinutes(1));
        cleanExpiredEntries(hourRequestCountMap, Duration.ofHours(1));
        cleanExpiredEntries(dayRequestCountMap, Duration.ofDays(1));
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally {
            super.finalize();
        }
    }
}



