package org.zeroagent.domain.core.aitask.engine.handler.forward;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时47分
 */
@Getter
public class TaskCancelActionImpl extends TaskActionImpl implements TaskCancelAction {
    private String reason;

    TaskCancelActionImpl(@Nullable UpdatableAiTask updatableAiTask) {
        super(ActionTypeEnum.FORWARD_CANCELED, updatableAiTask);
    }
    @Override
    public TaskAction withAiTask(@NotNull UpdatableAiTask updatableAiTask) {
        this.checkNotNull(updatableAiTask);
        TaskCancelActionImpl taskCancelAction = new TaskCancelActionImpl(updatableAiTask);
        taskCancelAction.reason = reason;
        return taskCancelAction;
    }
    @Override
    public TaskCancelAction withReason(String reason, @Nullable UpdatableAiTask updatableAiTask) {
        this.checkNullable(updatableAiTask);
        Asserts.notBlank(reason, "cancel reason must not be blank");
        TaskCancelActionImpl taskCancelAction = new TaskCancelActionImpl(updatableAiTask);
        taskCancelAction.reason = reason;
        return taskCancelAction;
    }

}
