package com.fan.reptile;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @author zhang_fan
 * @since 2022/4/12 上午 11:50
 */
public class ReptileTaskExecutor {

    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadPoolTaskExecutor();

    static {
        TASK_EXECUTOR.setCorePoolSize(20);
        TASK_EXECUTOR.setMaxPoolSize(64);
        TASK_EXECUTOR.setKeepAliveSeconds(1000);
        TASK_EXECUTOR.setQueueCapacity(1000);
        TASK_EXECUTOR.setThreadNamePrefix("reptile-exec");
        TASK_EXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        TASK_EXECUTOR.initialize();
    }

    public static Future<?> submit(Runnable runnable) {
        return TASK_EXECUTOR.submit(runnable);
    }
}
