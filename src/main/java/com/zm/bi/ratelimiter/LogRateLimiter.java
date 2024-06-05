package com.zm.bi.ratelimiter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;

public class LogRateLimiter {
    /**
     * 每分钟限制请求数
     */
    private static final long PERMITS_PER_MINUTE = 60;

    /**
     * 请求日志计数器，k-为请求的时间（秒），value当前时间的请求数量
     */
    private final TreeMap<Long,Integer> requestLogCountMap = new TreeMap<>();

    public synchronized boolean tryAcquire(){
        //最小时间粒度为s
        long currentTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        //获取当前窗口的请求总数
        int currentWindowCount = getCurrentWindowCount(currentTimestamp);
        if(currentWindowCount >= PERMITS_PER_MINUTE){
            return false;
        }
        //请求成功，将当前请求日志加入到日志中
        requestLogCountMap.merge(currentTimestamp,1,Integer::sum);
        return true;
    }

    private int getCurrentWindowCount(long currentTime){
        //计算出窗口的开始位置时间
        long startTime = currentTime -59;
        //遍历当前存储的计数器，删除无效的子窗口计数器，并累加当前窗口中的所有计数器之和
        return requestLogCountMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= startTime)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }
}
