package org.zeroagent.domain.core.aitask.engine.handler.forward;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.model.TaskBizResult;
import org.zeroagent.domain.core.aitask.model.TaskBizVars;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 * AI任务调度 - 步进模型
 * @author Nuk3m1
 * @version 2026年04月29日  15时46分
 */
public interface TaskAction {
    TaskStillAction KEEP_STILL = new TaskStillActionImpl(null);
    TaskAction       FORWARD_FINISHED = new TaskActionImpl(ActionTypeEnum.FORWARD_FINISHED, null);
    TaskCancelAction FORWARD_CANCELED = new TaskCancelActionImpl(null);
    TaskRollbackAction ROLLBACK = new TaskRollbackActionImpl(null);

    ActionTypeEnum getType();

    @Nullable
    UpdatableAiTask getUpdatableAiTask();

    TaskAction withAiTask(@NotNull UpdatableAiTask updatableAiTask);

    default TaskAction updateBizResult(TaskBizResult taskBizResult) {
        return this.withAiTask(new UpdatableAiTask().setBizResult(taskBizResult));
    }
    default TaskAction updateBizStatusAndResult(String bizStatus, TaskBizResult taskBizResult) {
        return this.withAiTask(new UpdatableAiTask().setBizStatus(bizStatus).setBizResult(taskBizResult));
    }
    default TaskAction updateBizStatusAndVars(String bizStatus, TaskBizVars taskBizVars) {
        return this.withAiTask(new UpdatableAiTask().setBizStatus(bizStatus).setBizVars(taskBizVars));
    }
    default TaskAction updateBizVars(TaskBizVars taskBizVars) {
        return this.withAiTask(new UpdatableAiTask().setBizVars(taskBizVars));
    }
    default TaskAction updateBizVarsAndBizResult(TaskBizVars taskBizVars, TaskBizResult taskBizResult) {
        return this.withAiTask(new UpdatableAiTask().setBizVars(taskBizVars).setBizResult(taskBizResult));
    }
    static TaskAction cancel(String reason) {
        return FORWARD_CANCELED.withReason(reason);
    }

    static TaskAction rollback(String reason) {
        return ROLLBACK.withReason(reason);
    }

    static TaskAction retry(String retryName, String reason) {
        return KEEP_STILL.withRetry(retryName, reason);
    }


    enum ActionTypeEnum {
        KEEP_STILL,
        FORWARD_FINISHED,
        FORWARD_CANCELED,
        ROLLBACK;
        public boolean isForwardFinished() {
            return this == FORWARD_FINISHED;
        }
    }
}
