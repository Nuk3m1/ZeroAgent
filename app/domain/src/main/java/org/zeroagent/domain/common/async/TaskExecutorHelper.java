package org.zeroagent.domain.common.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.stereotype.Component;
import org.zeroagent.common.utils.concurrent.ThreadPools;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月11日  19时05分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TaskExecutorHelper {
    //TODO 后续实现具体通知渠道后，可在拒绝策略中发送APP通知
//private final AppAlertHelper appAlertHelper;
    private final RejectedExecutionHandler callerRunsHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    public ThreadPoolTaskExecutorCustomizer rejectedHandlerCustomizer(String threadPoolName) {
        var rejectedExecutionHandler = ThreadPools.rejectedHandler(threadPoolName,
                (r, e) -> {
                    log.warn("Task {} rejected from {}, {}", r, e, threadPoolName);
                    callerRunsHandler.rejectedExecution(r, e);
                });
        return taskExecutor -> taskExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
    }
}
