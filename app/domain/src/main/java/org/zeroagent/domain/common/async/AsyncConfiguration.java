package org.zeroagent.domain.common.async;



import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 集中式配置异步线程池
 * 禁止配置全局异步池，每个业务使用线程池需自行配置，互相独立
 * @author Nuk3m1
 * @version 2026年04月09日  14时17分
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AsyncConfiguration {
    private final Tracer tracer;
    private final TaskExecutorHelper taskExecutorHelper;
    /**
     * 存储所有线程池的执行器,便于统一关闭
     */
    private final List<ThreadPoolTaskExecutor> executors = new ArrayList<>();


    @Bean(name = AsyncPools.CARD_SCHEDULER_LOAD_POOL)
    public ThreadPoolTaskExecutor cardSchedulerLoadPool() {
        ThreadPoolTaskExecutorCustomizer customizer = taskExecutorHelper.rejectedHandlerCustomizer(AsyncPools.CARD_SCHEDULER_LOAD_POOL);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutorBuilder()
                .queueCapacity(32)
                .corePoolSize(16)
                .maxPoolSize(16)
                .allowCoreThreadTimeOut(true)
                .keepAlive(Duration.ofSeconds(60))
                .awaitTermination(true)
                .awaitTerminationPeriod(Duration.ofSeconds(30))
                .threadNamePrefix("card-scheduler-load-")
                .customizers(customizer)
                .build();
        executor.afterPropertiesSet();
        executors.add(executor);
        return new TraceAwareThreadPoolTaskExecutor(executor, tracer);
    }

    @Bean(name = AsyncPools.CARD_SCHEDULER_EXECUTE_POOL)
    public ThreadPoolTaskExecutor cardSchedulerExecutePool() {
        ThreadPoolTaskExecutorCustomizer customizer = taskExecutorHelper.rejectedHandlerCustomizer(AsyncPools.CARD_SCHEDULER_EXECUTE_POOL);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutorBuilder()
                .queueCapacity(32)
                .corePoolSize(16)
                .maxPoolSize(16)
                .allowCoreThreadTimeOut(true)
                .keepAlive(Duration.ofSeconds(60))
                .awaitTermination(true)
                .awaitTerminationPeriod(Duration.ofSeconds(30))
                .threadNamePrefix("card-scheduler-execute-")
                .customizers(customizer)
                .build();
        executor.afterPropertiesSet();
        executors.add(executor);
        return new TraceAwareThreadPoolTaskExecutor(executor, tracer);
    }




    /**
     * 优雅关闭所有线程池
     */
    private void gracefulShutdownAllExecutors() {
        log.info("开始关闭 {} 个线程池", executors.size());
        for (ThreadPoolTaskExecutor executor : executors) {
            String threadNamePrefix = executor.getThreadNamePrefix();
            try {
                //  1. 停止接受新任务
                executor.shutdown();
                log.info("线程池 {} 已停止接受新任务", threadNamePrefix);

                // 2. 等待正在执行的任务完成
                if (!executor.getThreadPoolExecutor().awaitTermination(5, TimeUnit.MINUTES)) {
                    log.warn("线程池 {} 在 5 分钟内未能正常关闭，尝试强行关闭", threadNamePrefix);
                    // 3.强行关闭
                    executor.getThreadPoolExecutor().shutdownNow();

                    //4. 再次等待
                    if (!executor.getThreadPoolExecutor().awaitTermination(10, TimeUnit.SECONDS)) {
                        log.error("线程池 {} 无法强制关闭",  threadNamePrefix);
                    } else {
                        log.info("线程池 {} 已强制关闭", threadNamePrefix);
                    }
                }
            } catch (InterruptedException e) {
                log.error("关闭线程池时 {} 发生中断异常", threadNamePrefix, e);
                // 恢复中断
                Thread.currentThread().interrupt();
                // 强行关闭线程池
                executor.getThreadPoolExecutor().shutdownNow();
            } catch (Exception e) {
                log.error("关闭线程池 {} 时发生异常", threadNamePrefix, e);
            }
        }
        log.info("所有线程池关闭完成");
    }


    /**
     * 监控线程池信息
     */
    public void logThreadPoolStatus() {
        for (ThreadPoolTaskExecutor executor : executors) {
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
            log.info("线程池 {} 状态: 核心线程数={}, 最大线程数={}, 活跃线程数={}, 队列大小={}, 已完成任务数={}",
                    executor.getThreadNamePrefix(),
                    threadPoolExecutor.getCorePoolSize(),
                    threadPoolExecutor.getMaximumPoolSize(),
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getQueue().size(),
                    threadPoolExecutor.getCompletedTaskCount());
        }
    }
}
