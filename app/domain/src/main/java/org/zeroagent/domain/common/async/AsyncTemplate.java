package org.zeroagent.domain.common.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.support.notification.app.AppSyncAlertHelper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步函数执行模版 (线程池在这里集中定义为SpringBean)
 * 所有异步线程池操作必须通过这个类进行
 * @author Nuk3m1
 * @version 2026年04月12日  14时01分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncTemplate {
    private final ApplicationContext applicationContext;
    private final AppSyncAlertHelper appSyncAlertHelper;

    /**
     * 获取线程池实例
     * @param threadPoolName 线程池名称
     * @return 线程池实例
     */
    public AsyncTaskExecutor getTaskExecutor(String threadPoolName) {
        try {
            return applicationContext.getBean(threadPoolName, AsyncTaskExecutor.class);
        } catch (BeansException e) {
            log.error("[execute][not_such_bean]name = {}", threadPoolName);
            appSyncAlertHelper.alertText("[AsyncTemplate][execute][not_such_bean]name={}", threadPoolName, e);
            throw new SysException(CommonErrorCode.UNSPECIFIED, "通过线程池名称未能找到线程池实例");
        }
    }

    /**
     * 静默提交函数执行 (不抛出异常)
     * @param threadPoolName 线程池名称 (必须为Spring Bean)
     * @param command 待执行函数
     */
    public void submitSilently(@NotNull String threadPoolName, @NotNull Runnable command) {
        try {
            this.execute(threadPoolName, command);
        } catch (Exception ignore) {
            // 静默，不做任何处理
        }
    }

    /**
     * 检查线程池阻塞队列是否有可用容量
     * @param threadPoolName 线程池名称
     * @return 是否拥有可用容量
     */
    public boolean checkExecutorQueueCapacity(@NotNull String threadPoolName) {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.getTaskExecutor(threadPoolName);
        BlockingQueue<Runnable> queue = executor.getThreadPoolExecutor().getQueue();
        return queue.remainingCapacity() > 0;
    }

    public void execute(@NotNull String threadPoolName, @NotNull Runnable command) {
        Asserts.notBlank(threadPoolName, "线程池名称不能为空");
        Asserts.notNull(command, "执行函数不能为空");
        final Executor executor;
        try {
            executor = applicationContext.getBean(threadPoolName, Executor.class);
        } catch (BeansException e) {
            log.error("[execute][not_such_bean]name = {}", threadPoolName, e);
            appSyncAlertHelper.alertText("[AsyncTemplate][execute][not_such_bean] name = {}", threadPoolName, e);
            throw new SysException(CommonErrorCode.ILLEGAL_PARAM, "通过线程池名称未能找到线程池实例");
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 如果当前存在事务，则注册事务回调钩子，等事务提交后执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    AsyncTemplate.this.doExecute(threadPoolName, command, executor);
                }
            });
        } else {
            this.doExecute(threadPoolName, command, executor);
        }
    }

    private void doExecute(@NotNull String threadPoolName,@NotNull Runnable command, Executor executor) {
        try {
            executor.execute(command);
        } catch (RejectedExecutionException e) {
            log.error("[execute][thread_pool_is_full] name = {}", threadPoolName);
            appSyncAlertHelper.alertText("[AsyncTemplate][execute][thread_pool_is_full] name = {}", threadPoolName, e);
            throw new BizException(CommonErrorCode.SLA_LIMITED);
        } catch (Exception e) {
            log.warn("[execute][command_error] name = {}", threadPoolName, e);
            appSyncAlertHelper.alertText("[AsyncTemplate][execute][command_error] name = {}", threadPoolName, e);
            throw e;
        }
    }
}
