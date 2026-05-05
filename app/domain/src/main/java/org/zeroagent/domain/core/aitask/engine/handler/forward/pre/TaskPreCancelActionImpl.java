package org.zeroagent.domain.core.aitask.engine.handler.forward.pre;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时30分
 */
@Getter
public class TaskPreCancelActionImpl extends TaskPreActionImpl implements TaskPreCancelAction {
    private String reason;

    TaskPreCancelActionImpl(@Nullable UpdatableAiTask updatableAiTask) {
        super(ActionTypeEnum.FORWARD_CANCELED, updatableAiTask);
    }

    @Override
    public TaskPreCancelAction withAiTask(@NotNull UpdatableAiTask updatableAiTask) {
        super.checkNotNull(updatableAiTask);
        TaskPreCancelActionImpl taskPreCancelAction = new TaskPreCancelActionImpl(updatableAiTask);
        taskPreCancelAction.reason = reason;
        return taskPreCancelAction;
    }

    @Override
    public TaskPreCancelAction withReason(String reason, @Nullable UpdatableAiTask updatableAiTask) {
        super.checkNullable(updatableAiTask);
        Asserts.notBlank(reason, "cancel reason must not be blank");
        TaskPreCancelActionImpl taskPreCancelAction = new TaskPreCancelActionImpl(updatableAiTask);
        taskPreCancelAction.reason = reason;
        return taskPreCancelAction;
    }
}
