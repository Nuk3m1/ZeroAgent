package org.zeroagent.domain.core.aitask.engine.handler.forward.pre;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时29分
 */
public interface TaskPreCancelAction extends TaskPreAction {
    default TaskPreCancelAction withReason(String reason) {
        return this.withReason(reason, null);
    }
    TaskPreCancelAction withReason(String reason, @Nullable UpdatableAiTask updatableAiTask);

    String getReason();
}
