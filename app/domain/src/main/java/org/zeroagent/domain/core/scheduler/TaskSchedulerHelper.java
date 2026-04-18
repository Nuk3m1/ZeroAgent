package org.zeroagent.domain.core.scheduler;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.stereotype.Component;
import org.zeroagent.common.utils.concurrent.ThreadPools;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月09日  15时33分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TaskSchedulerHelper {
    // TODO 后续实现具体通知渠道后，可在拒绝策略中发送APP通知
//    private final AppAlertHelper appAlertHelper;
    private final RejectedExecutionHandler callerRunsHandler = new ThreadPoolExecutor.CallerRunsPolicy();
    /**
     * 线程池拒绝策略自定义处理函数
     * @param threadPoolName 线程池名称
     * @return 线程池自定义函数
     */
    public ThreadPoolTaskSchedulerCustomizer rejectedHandlerCustomizer(String threadPoolName) {
        var rejectedExecutionHandler = ThreadPools.rejectedHandler(threadPoolName,
                (r, e) -> {
                    log.warn("Task {} rejected from {}, {}", r, e, threadPoolName);
                    callerRunsHandler.rejectedExecution(r, e);
                });
        return taskScheduler -> taskScheduler.setRejectedExecutionHandler(rejectedExecutionHandler);
    }

}
