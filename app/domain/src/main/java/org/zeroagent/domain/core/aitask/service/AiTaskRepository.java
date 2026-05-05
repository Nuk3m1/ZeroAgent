package org.zeroagent.domain.core.aitask.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskExecInfo;
import org.zeroagent.domain.core.aitask.error.AiTaskErrorCode;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskExecStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  14时26分
 */
public interface AiTaskRepository {
    long create(AiTask aiTask);

    AiTask createIdempotent(AiTask aiTask);


    long countInRunning(AiTaskType aiTaskType, String bizType);

    /**
     * 通过主键ID - 更新执行状态
     * @param id 主键ID
     * @param status 执行状态
     */
    void updateExecuteStatusById(long id, AiTaskExecStatus status);

    void updateById(AiTask aiTask);

    Optional<AiTask> queryOptionalById(long id);

    Optional<AiTask> queryOptionalByIdAndTaskId(long id, long taskId);

    @NotNull
    default AiTask queryById(long id) {
        return this.queryOptionalById(id).orElseThrow(() -> new SysException(AiTaskErrorCode.TASK_NOT_EXISTS));
    }

    void startById(long id, @Nullable UpdatableAiTask aiTask, AiTaskExecInfo execInfo);

    void rollbackRunningToCreatedById(long id, @Nullable UpdatableAiTask aiTask, AiTaskExecInfo execInfo);

    void cancelById(long id, String reason, @Nullable UpdatableAiTask aiTask, AiTaskExecInfo execInfo);

    void finishById(long id, ZonedDateTime createdAt, @Nullable UpdatableAiTask aiTask, AiTaskExecInfo execInfo);

    void dryRunById(long id, @Nullable UpdatableAiTask aiTask, @Nullable AiTaskExecInfo execInfo);

    void completeById(long id, @Nullable UpdatableAiTask aiTask, AiTaskExecInfo execInfo);

    default void dryRunById(long id, UpdatableAiTask aiTask) {
        this.dryRunById(id, aiTask, null);
    }

    Optional<AiTask> queryByUnique(AiTaskType taskType, String bizType, String bizNo, String subBizNo);


    /**
     * 查询可执行任务
     * @param aiTaskType 任务类型
     * @param bizType    业务类型
     * @param limit      查询数量限制
     * @return           任务列表
     */
    List<AiTask> queryWaitingForAutoExec(AiTaskType aiTaskType, String bizType, int limit);


}
