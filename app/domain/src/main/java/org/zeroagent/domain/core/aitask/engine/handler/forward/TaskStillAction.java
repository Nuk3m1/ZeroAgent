package org.zeroagent.domain.core.aitask.engine.handler.forward;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时47分
 */
public interface TaskStillAction extends TaskAction {
    default TaskStillAction withRetry(String retryName, String reason) {
        return this.withRetry(retryName, reason, null);
    }
    TaskStillAction withRetry(String retryName, String reason, @Nullable UpdatableAiTask updatableAiTask);
    String getRetryName();
    String getReason();
}
