package org.zeroagent.domain.core.aitask.engine.handler.forward;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时48分
 */
@Getter
public class TaskRollbackActionImpl extends TaskActionImpl implements TaskRollbackAction {
    private String reason;
    TaskRollbackActionImpl(@Nullable UpdatableAiTask updatableAiTask) {
        super(ActionTypeEnum.ROLLBACK, updatableAiTask);
    }

    @Override
    public TaskAction withAiTask(@NotNull UpdatableAiTask updatableAiTask) {
        this.checkNotNull(updatableAiTask);
        TaskRollbackActionImpl taskRollbackAction = new TaskRollbackActionImpl(updatableAiTask);
        taskRollbackAction.reason = reason;
        return taskRollbackAction;
    }

    @Override
    public TaskRollbackAction withReason(String reason, @Nullable UpdatableAiTask updatableAiTask) {
        this.checkNullable(updatableAiTask);
        Asserts.notBlank(reason, "rollback reason must not be blank");
        TaskRollbackActionImpl taskRollbackAction = new TaskRollbackActionImpl(updatableAiTask);
        taskRollbackAction.reason = reason;
        return taskRollbackAction;
    }
}
