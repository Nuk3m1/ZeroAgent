package org.zeroagent.common.utils.concurrent;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.ThreadPoolRejectedException;

import java.util.concurrent.*;

/**
 * 线程池工具类
 */
@Slf4j
@UtilityClass
public class ThreadPools {
    /**
     * CUP核数（对于CPU密集型任务，2~4倍的线程数量可获得最佳性能）
     */
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public RejectedExecutionHandler rejectedHandler(String threadPoolName) {
        return (r, executor) -> {
            log.error("Task {} rejected from {},{}", r, executor, threadPoolName);
            throw new ThreadPoolRejectedException(CommonErrorCode.SLA_LIMITED);
        };
    }

    public RejectedExecutionHandler rejectedHandler(String threadPoolName, RejectedExecutionHandler handler) {
        return (runnable, executor) -> {
            log.error("Task {} rejected from {},{}", runnable, executor, threadPoolName);
            handler.rejectedExecution(runnable, executor);
            throw new ThreadPoolRejectedException(CommonErrorCode.SLA_LIMITED);
        };
    }
    public static ExecutorService newSingleton(String threadPoolName) {
        // queueSize应该为Integer.MAX_VALUE
        return newFixedThreadPool(1, 1024, threadPoolName);
    }
    /**
     * 创建有限线程数的线程池
     * <p/>
     * 特别注意：必须要在Spring的环境变量装载之后才能调用，
     * 否则方法内依赖的sofa-common-tools.jar里的类会提前初始化中间件日志，导致日志配置内的变量缺失。
     * @param nThreads       线程数
     * @param queueSize      队列大小
     * @param threadPoolName 线程池名称（同时会作为每个线程的前缀名称）
     * @return {@link ExecutorService}
     */
    public static ExecutorService newFixedThreadPool(int nThreads, int queueSize, String threadPoolName) {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);
        ThreadFactory tf = new NamedThreadFactory(threadPoolName, true);
        RejectedExecutionHandler reh = rejectedHandler(threadPoolName);
        return new ThreadPoolExecutor(nThreads, nThreads, 60L, TimeUnit.SECONDS, queue, tf, reh);
    }
    public static ExecutorService newScheduledThreadPool(int corePoolSize, String threadPoolName) {
        ThreadFactory tf = new NamedThreadFactory(threadPoolName, true);
        RejectedExecutionHandler reh = rejectedHandler(threadPoolName);
        return new ScheduledThreadPoolExecutor(corePoolSize, tf, reh);
    }
    /**
     *  优雅关闭 线程池
     */
    public void shutdownGracefully(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    log.error("ThreadPool did not terminate!");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
