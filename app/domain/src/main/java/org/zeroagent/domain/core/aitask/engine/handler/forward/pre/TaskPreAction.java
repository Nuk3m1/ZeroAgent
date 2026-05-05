package org.zeroagent.domain.core.aitask.engine.handler.forward.pre;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.model.TaskBizVars;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时12分
 */
public interface TaskPreAction {
    TaskPreAction       KEEP_STILL       = new TaskPreActionImpl(ActionTypeEnum.KEEP_STILL, null);
    TaskPreAction       FORWARD_RUNNING  = new TaskPreActionImpl(ActionTypeEnum.FORWARD_RUNNING, null);
    TaskPreAction       FORWARD_FINISHED = new TaskPreActionImpl(ActionTypeEnum.FORWARD_FINISHED, null);
    TaskPreCancelAction FORWARD_CANCELED = new TaskPreCancelActionImpl(null);


    ActionTypeEnum getType();

    @Nullable
    UpdatableAiTask getUpdatableAiTask();

    TaskPreAction withAiTask(@NotNull UpdatableAiTask updatableAiTask);

    default TaskPreAction updateBizStatus(String bizStatus) {
        return this.withAiTask(new UpdatableAiTask().setBizStatus(bizStatus));
    }

    default TaskPreAction updateBizVars(TaskBizVars bizVars) {
        return this.withAiTask(new UpdatableAiTask().setBizVars(bizVars));
    }

    default TaskPreAction updateBizStatusAndVars(String bizStatus, TaskBizVars bizVars) {
        return this.withAiTask(new UpdatableAiTask().setBizStatus(bizStatus).setBizVars(bizVars));
    }

    default TaskPreAction updateBizExecInfo(TaskBizVars bizVars) {
        return this.withAiTask(new UpdatableAiTask().setBizVars(bizVars));
    }

    default boolean isKeepStill() {
        return getType().isKeepStill();
    }

    static TaskPreAction cancel(String reason) {
        return FORWARD_CANCELED.withReason(reason);
    }

    enum ActionTypeEnum {
        KEEP_STILL,
        FORWARD_RUNNING,
        FORWARD_FINISHED,
        FORWARD_CANCELED;

        private boolean isKeepStill() {
            return this == KEEP_STILL;
        }
    }
}
