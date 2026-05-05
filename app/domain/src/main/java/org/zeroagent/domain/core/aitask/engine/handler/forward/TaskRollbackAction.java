package org.zeroagent.domain.core.aitask.engine.handler.forward;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时47分
 */
public interface TaskRollbackAction extends TaskAction {
    default TaskRollbackAction withReason(String reason) {
        return this.withReason(reason, null);
    }
    TaskRollbackAction withReason(String reason, @Nullable UpdatableAiTask updatableAiTask);

    String getReason();
}
