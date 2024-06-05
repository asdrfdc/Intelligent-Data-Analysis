package com.zm.bi.utils;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.elasticsearch.common.recycler.Recycler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryUtil {

    private static AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * 创建并返回一个配置好的Retryer实例，用于重试策略
     *
     * @return 配置好的Retryer实例
     */
    public static <V> Retryer<V> createRetryer() {
        return RetryerBuilder.<V>newBuilder()
                .retryIfException(Predicates.<Throwable>instanceOf(ExecutionException.class))
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        // 如果当前重试包含异常，就打印重试次数和异常原因
                        if (attempt.hasException()) {
                            System.out.println("Retry attempt " + retryCount.incrementAndGet() + " failed with exception: " + attempt.getExceptionCause().getMessage());
                        }
                        // 如果没有异常表示重试成功了，打印重试次数和成功信息
                        else {
                            System.out.println("Retry attempt " + retryCount.incrementAndGet() + " succeeded with result: " + attempt.getResult());
                        }
                    }
                })
                .build();
    }
}
