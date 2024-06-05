package com.zm.bi.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("thread-" + count);
                count++;
                return thread;
            }
        };
        return new ThreadPoolExecutor(
                2,
                4,
                100,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(4),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
