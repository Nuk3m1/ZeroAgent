package org.zeroagent.domain.core.aitask.engine.handler.forward;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时46分
 */
public interface TaskCancelAction extends TaskAction {
    default TaskCancelAction withReason(String reason) {
        return this.withReason(reason, null);
    }
    TaskCancelAction withReason(String reason, @Nullable UpdatableAiTask updatableAiTask);

    String getReason();
}
