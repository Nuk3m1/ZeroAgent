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
public class TaskStillActionImpl extends TaskActionImpl implements TaskStillAction {
    private String retryName;
    private String reason;


    TaskStillActionImpl(@Nullable UpdatableAiTask updatableAiTask) {
            super(ActionTypeEnum.KEEP_STILL, updatableAiTask);
    }

    @Override
    public TaskAction withAiTask(@NotNull UpdatableAiTask updatableAiTask) {
        this.checkNotNull(updatableAiTask);
        TaskStillActionImpl taskStillAction = new  TaskStillActionImpl(updatableAiTask);
        taskStillAction.retryName = retryName;
        taskStillAction.reason = reason;
        return taskStillAction;
    }
    @Override
    public TaskStillAction withRetry(String name, String reason, @Nullable UpdatableAiTask updatableAiTask) {
        this.checkNullable(updatableAiTask);
        Asserts.notBlank(name, "retry name must not be blank");
        Asserts.notBlank(reason, "retry reason must not be blank");
        TaskStillActionImpl taskAction = new TaskStillActionImpl(updatableAiTask);
        taskAction.retryName = name;
        taskAction.reason = reason;
        return taskAction;
    }
}
