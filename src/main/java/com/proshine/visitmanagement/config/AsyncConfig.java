package com.proshine.visitmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 
 * @author System
 * @since 2024-01-01
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    
    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 2;
    
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 10;
    
    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 100;
    
    /**
     * 线程名前缀
     */
    private static final String THREAD_NAME_PREFIX = "VisitMgmt-Async-";
    
    /**
     * 线程空闲时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;
    
    /**
     * 配置异步任务线程池
     *
     * @return 线程池执行器
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：线程池维护线程的最少数量
        executor.setCorePoolSize(CORE_POOL_SIZE);
        
        // 最大线程数：线程池维护线程的最大数量
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        
        // 缓存队列：当核心线程数达到最大时，新任务会放在队列中排队等待执行
        executor.setQueueCapacity(QUEUE_CAPACITY);
        
        // 线程空闲时间：超过核心线程数的线程在空闲时间超过该值时会被销毁
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        
        // 线程名称前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        
        // 拒绝策略：当线程池和队列都满了，对新任务采取的处理策略
        // CallerRunsPolicy：由调用线程直接执行该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(30);
        
        // 线程池初始化
        executor.initialize();
        
        log.info("异步任务线程池配置完成: 核心线程数={}, 最大线程数={}, 队列容量={}", 
                 CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        
        return executor;
    }
    
    /**
     * 配置邮件发送专用线程池
     *
     * @return 邮件发送线程池
     */
    @Bean(name = "mailTaskExecutor")
    public Executor getMailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 邮件发送线程池配置（较小的线程池）
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("VisitMgmt-Mail-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("邮件发送线程池配置完成: 核心线程数=1, 最大线程数=3, 队列容量=50");
        
        return executor;
    }
    
    /**
     * 配置文件处理专用线程池
     *
     * @return 文件处理线程池
     */
    @Bean(name = "fileTaskExecutor")
    public Executor getFileTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 文件处理线程池配置
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(30);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("VisitMgmt-File-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("文件处理线程池配置完成: 核心线程数=2, 最大线程数=5, 队列容量=30");
        
        return executor;
    }
    
    /**
     * 异步任务异常处理器
     *
     * @return 异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
    
    /**
     * 自定义异步异常处理器
     */
    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable throwable, 
                                          java.lang.reflect.Method method, 
                                          Object... params) {
            log.error("异步任务执行异常 - 方法: {}.{}, 参数: {}", 
                     method.getDeclaringClass().getName(), 
                     method.getName(), 
                     params, 
                     throwable);
            
            // 这里可以添加异常通知逻辑，比如发送邮件、写入数据库等
            // 例如：发送异常通知邮件
            // notificationService.sendExceptionAlert(throwable, method, params);
        }
    }
}