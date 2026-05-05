package org.zeroagent.domain.core.aitask.engine;

import jodd.exception.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.domain.core.aitask.engine.handler.AiTaskHandler;
import org.zeroagent.domain.core.aitask.engine.handler.factory.AiTaskHandlerFactory;
import org.zeroagent.domain.core.aitask.engine.handler.forward.TaskAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.TaskCancelAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.TaskRollbackAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.TaskStillAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.post.TaskPostAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.pre.TaskPreAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.pre.TaskPreCancelAction;
import org.zeroagent.domain.core.aitask.engine.handler.transaction.AiTaskSynchronization;
import org.zeroagent.domain.core.aitask.engine.handler.transaction.AiTaskTransactionManager;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskConfig;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskConstant;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskExecInfo;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskExecStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;
import org.zeroagent.domain.core.aitask.service.AiTaskRepository;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时37分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiTaskEngineImpl implements AiTaskEngine {
    private final AiTaskRepository aiTaskRepository;
    private final AiTaskHandlerFactory aiTaskHandlerFactory;
    private final TransactionTemplate transactionTemplate;
    private final AppAlertHelper appAlertHelper;

    @Override
    public void execute(@NotNull AiTask aiTask) {
        long taskId = aiTask.getId();
        AiTaskType taskType = aiTask.getTaskType();
        String bizType = aiTask.getBizType();

        AiTaskConfig config = aiTaskHandlerFactory.getTaskConfig(taskType, bizType);

        log.info("[execute]taskId = {}, taskType = {}, bizType = {}", taskId, taskType, bizType);

        // 获取下级处理器 - 实现处理器接口
        AiTaskHandler taskHandler = aiTaskHandlerFactory.getHandler(taskType, bizType);
        if (taskHandler == null) {
            log.warn("[execute]aiTaskHandler is null, taskType = {}, bizType = {}", taskType, bizType);
            return;
        }
        // 设置 执行消息 用于挂载执行中信息 ExecInfo
        AiTaskExecInfo execInfo = aiTask.getExecInfo()
                .setExecTime(LocalDateTime.now(ZoneId.systemDefault()))
                .incExecCount();

        // 对于 AiTask 的三种状态，进行处理: 1. CREATED 2.RUNNING 3. FINISHED/CANCELED
        try {
            if (this.checkSysTimeout(aiTask)) {
                // 任务超时 则 直接取消
                UpdatableAiTask updatableAiTask = new UpdatableAiTask().setBizStatus(AiTaskConstant.BIZ_STATUS_TIMEOUT);
                aiTaskRepository.cancelById(taskId, "TIMEOUT", updatableAiTask, execInfo);
                aiTask = aiTaskRepository.queryById(taskId);
            } else if (execInfo.getExecCount() > config.getExecTryLimit()) {
                // 任务超过最大执行次数，直接取消
                UpdatableAiTask updatableAiTask = new UpdatableAiTask().setBizStatus(AiTaskConstant.BIZ_STATUS_TRYOUT);
                aiTaskRepository.cancelById(taskId, "TRYOUT", updatableAiTask, execInfo);
            }

            // 处理 CREATED 任务
            if (aiTask.getTaskStatus() == AiTaskStatus.CREATED) {
                // 预处理
                try {
                    // 初始化任务更新同步函数
                    AiTaskTransactionManager.initSynchronization();
                    // 核心处理
                    TaskPreAction preAction = taskHandler.preHandle(aiTask);

                    if (preAction == null) {
                        preAction = TaskPreAction.KEEP_STILL;
                    }
                    @Nullable UpdatableAiTask updatableAiTask = preAction.getUpdatableAiTask();
                    if (preAction.isKeepStill() && updatableAiTask == null && AiTaskTransactionManager.isSynchronizationEmpty()) {
                        aiTaskRepository.updateExecuteStatusById(aiTask.getId(), AiTaskExecStatus.WAITING);
                        log.warn("[execute][CREATED -> KEEP_STILL] no changes, taskId = {}, taskType = {}, bizType = {}", taskId, taskType, bizType);
                        return;
                    }
                    if (updatableAiTask == null) {
                        updatableAiTask = new  UpdatableAiTask();
                    }
                    if (updatableAiTask.getStartedAt() == null) {
                        updatableAiTask.setStartedAt(ZonedDateTime.now());
                    }
                    switch (preAction.getType()) {
                        // 状态不变
                        case KEEP_STILL -> this.dryRun(taskId, updatableAiTask, execInfo);
                        // 更新到 RUNNING
                        case FORWARD_RUNNING -> this.start(taskId, updatableAiTask, execInfo);
                        // 更新到 FINISHED
                        case FORWARD_FINISHED -> {
                            if (updatableAiTask.getFinishedAt() == null) {
                                updatableAiTask.setFinishedAt(ZonedDateTime.now());
                            }
                            this.finish(aiTask, updatableAiTask, execInfo);
                        }
                        // 更新到 CANCELED
                        case FORWARD_CANCELED -> {
                            TaskPreCancelAction taskPreCancelAction = (TaskPreCancelAction) preAction;
                            this.cancel(taskId, taskPreCancelAction.getReason(), updatableAiTask, execInfo);
                        }
                        // 兜底
                        default -> throw new SysException("no such pre action : " + preAction.getType());
                    }

                } finally {
                    AiTaskTransactionManager.clearSynchronizations();
                }
                aiTask = aiTaskRepository.queryById(taskId);
            }
            // 处理 RUNNING 任务
            if (aiTask.getTaskStatus() == AiTaskStatus.RUNNING) {
                try {
                    // 初始化任务更新同步函数
                    AiTaskTransactionManager.initSynchronization();
                    // 核心处理
                    TaskAction taskAction = taskHandler.handle(aiTask);
                    if (taskAction == null) {
                        taskAction = TaskAction.KEEP_STILL;
                    }
                    @Nullable UpdatableAiTask updatableAiTask = taskAction.getUpdatableAiTask();
                    switch (taskAction.getType()) {
                        // 状态不变
                        case KEEP_STILL -> {
                            TaskStillAction taskStillAction = (TaskStillAction) taskAction;
                            String retryName = taskStillAction.getRetryName();
                            if (updatableAiTask == null && AiTaskTransactionManager.getSynchronizations().isEmpty() && retryName == null) {
                                aiTaskRepository.updateExecuteStatusById(aiTask.getId(), AiTaskExecStatus.WAITING);
                                return;
                            }
                            this.dryRun(taskId, updatableAiTask, execInfo);
                            return;
                        }
                        // 更新到 FINISHED
                        case FORWARD_FINISHED -> {
                            if (updatableAiTask == null) {
                                updatableAiTask = new UpdatableAiTask();
                            }
                            if (updatableAiTask.getFinishedAt() == null) {
                                updatableAiTask.setFinishedAt(ZonedDateTime.now());
                            }
                            this.finish(aiTask, updatableAiTask, execInfo);
                        }
                        // 更新到 CANCELED
                        case FORWARD_CANCELED -> {
                            TaskCancelAction taskCancelAction = (TaskCancelAction) taskAction;
                            this.cancel(taskId, taskCancelAction.getReason(), updatableAiTask, execInfo);
                        }
                        // 回滚到最初状态
                        case ROLLBACK -> {
                            TaskRollbackAction taskRollbackAction = (TaskRollbackAction) taskAction;
                            // 判断是否超过最大回滚次数
                            if (execInfo.exceedMaxRollbackCount(config.getMaxRollbackCount())) {
                                // 超过最大回滚次数 -> 取消任务
                                if (updatableAiTask == null) {
                                    updatableAiTask = new UpdatableAiTask();
                                }
                                if (StringUtils.isBlank(taskRollbackAction.getReason())) {
                                    this.cancel(taskId, "exceed max rollback count", updatableAiTask, execInfo);
                                } else {
                                    this.cancel(taskId, taskRollbackAction.getReason(), updatableAiTask, execInfo);
                                }
                            } else {
                                // 未超过最大回滚次数 -> 执行回滚操作
                                execInfo.incRollbackCount()
                                        .setRollbackTime(LocalDateTime.now())
                                        .setRollbackReason(taskRollbackAction.getReason());
                                this.rollbackRunningToCreated(taskId, updatableAiTask, execInfo);
                                // TODO 实现回滚告警 taskAlertHelper
                                return;
                            }
                        }
                        default -> throw new SysException("no such action: " + taskAction.getType());
                    }
                } finally {
                    AiTaskTransactionManager.clearSynchronizations();
                }
                aiTask = aiTaskRepository.queryById(taskId);
            }

            // 更新 FINISHED / CANCELED 任务
            if (aiTask.getTaskStatus().isFinal()) {
                try {
                    // 初始化任务更新同步函数
                    AiTaskTransactionManager.initSynchronization();

                    // 若任务已经完成，但没有完成时间，则设置完成时间
                    if (aiTask.getTaskStatus() == AiTaskStatus.FINISHED && aiTask.getFinishedAt() == null) {
                        this.finish(aiTask, null, execInfo);
                        aiTask = aiTaskRepository.queryById(taskId);
                    }
                    // 后续处理 (进行回调逻辑)
                    TaskPostAction taskPostAction = taskHandler.postHandle(aiTask);
                    if (taskPostAction == null) {
                        taskPostAction = TaskPostAction.COMPLETE;
                    }
                    @Nullable UpdatableAiTask updatableAiTask = taskPostAction.getUpdatableAiTask();
                    switch (taskPostAction.getType()) {
                        // 状态不变
                        case KEEP_STILL -> {
                            if (updatableAiTask == null && AiTaskTransactionManager.getSynchronizations().isEmpty()) {
                                aiTaskRepository.updateExecuteStatusById(aiTask.getId(), AiTaskExecStatus.WAITING);
                                return;
                            }
                            this.dryRun(taskId, updatableAiTask, execInfo);
                        }
                        // 完结任务
                        case COMPLETE -> {
                            this.complete(aiTask, updatableAiTask, execInfo, config);
                            // 对于已经完结的任务 ， 分情况告警
                            if (aiTask.getTaskStatus() == AiTaskStatus.CANCELED) {
                                // 任务取消 -> 告警
                                if (updatableAiTask != null) {
                                    // 存在更新数据，则更新 任务实例 为最新
                                    aiTask = aiTaskRepository.queryById(taskId);
                                }
                                // TODO 此处进行告警 taskAlertHelper
                            }
                            // TODO 对于存在 回滚/重试 次数的任务进行告警
                        }
                        default -> throw new SysException("no such post action : " + taskPostAction.getType());
                    }
                } finally {
                    AiTaskTransactionManager.clearSynchronizations();
                }
            }

        } catch (Exception e) {
            log.error("[execute][AiTaskEngineImpl] taskId = {}, taskType = {}, bizType = {}", taskId, taskType, bizType, e);
            execInfo.incErrorCount()
                    .setErrorTime(LocalDateTime.now())
                    .setErrorMsg(ExceptionUtil.exceptionStackTraceToString(e));
            aiTaskRepository.dryRunById(taskId, null, execInfo);
            // TODO 告警处理
        }

    }

    /**
     *  判断任务是否超时
     *  TODO 后续 aitask 挂载 TaskSysParams 字段，实现 不同类型 任务的 不同颗粒度 超时时间设置
     * @param aiTask 任务
     * @return 是否超时
     */
    private boolean checkSysTimeout(AiTask aiTask) {
        ZonedDateTime now = ZonedDateTime.now();
        {
            // 1. 从任务创建时间判断是否超时, 目前统一为20 Min
            Duration timeout = Duration.ofMinutes(20);
            if (Duration.between(aiTask.getCreatedAt(), now).compareTo(timeout) > 0) {
                return true;
            }
        }
        {
            // 2.从任务开始执行时间判断是否超时，目前统一为15 Min
            Duration timeout = Duration.ofMinutes(15);
            if (aiTask.getStartAt() != null && Duration.between(aiTask.getStartAt(), now).compareTo(timeout) > 0) {
                return true;
            }
        }
        // 全局兜底： 1 天
        Duration timeout = Duration.ofDays(1);
        return Duration.between(aiTask.getCreatedAt(), now).compareTo(timeout) > 0;
    }

    /**
     * 空跑任务
     * @param taskId                任务ID
     * @param updatableAiTask       任务更新数据
     * @param execInfo              任务执行数据
     */
    private void dryRun(long taskId, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        List<AiTaskSynchronization> synchronizations = AiTaskTransactionManager.getSynchronizations();
        if (synchronizations.isEmpty()) {
            aiTaskRepository.dryRunById(taskId, updatableAiTask, execInfo);
        } else {
            transactionTemplate.executeWithoutResult(status -> {
                AiTaskTransactionManager.getSynchronizations().forEach(AiTaskSynchronization::inCommit);
                aiTaskRepository.dryRunById(taskId, updatableAiTask, execInfo);
            });
        }
    }




    /**
     * 开始任务
     * @param taskId            任务ID
     * @param updatableAiTask   任务更新数据
     * @param execInfo          任务执行数据
     */
    private void start(long taskId, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        List<AiTaskSynchronization> synchronizations = AiTaskTransactionManager.getSynchronizations();
        if (synchronizations.isEmpty()) {
            aiTaskRepository.startById(taskId, updatableAiTask, execInfo);
        } else {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
               AiTaskTransactionManager.getSynchronizations().forEach(AiTaskSynchronization::inCommit);
               aiTaskRepository.startById(taskId, updatableAiTask, execInfo);
            });
        }
    }



    /**
     * 回滚任务  RUNNING -> CREATED
     * @param taskId                任务实例
     * @param updatableAiTask       任务更新数据
     * @param execInfo              任务执行数据
     */
    private void rollbackRunningToCreated(long taskId, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        List<AiTaskSynchronization> synchronizations = AiTaskTransactionManager.getSynchronizations();
        if (synchronizations.isEmpty()) {
            aiTaskRepository.rollbackRunningToCreatedById(taskId, updatableAiTask, execInfo);
        } else {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                AiTaskTransactionManager.getSynchronizations().forEach(AiTaskSynchronization::inCommit);
                aiTaskRepository.rollbackRunningToCreatedById(taskId, updatableAiTask, execInfo);
            });
        }
    }


    /**
     * 完成任务
     * @param aiTask            任务实例
     * @param updatableAiTask   任务更新数据
     * @param execInfo          任务执行数据
     */
    private void finish(AiTask aiTask, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        List<AiTaskSynchronization> synchronizations = AiTaskTransactionManager.getSynchronizations();
        if (synchronizations.isEmpty()) {
            aiTaskRepository.finishById(aiTask.getId(), aiTask.getCreatedAt(), updatableAiTask, execInfo);
        } else {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
               AiTaskTransactionManager.getSynchronizations().forEach(AiTaskSynchronization::inCommit);
               aiTaskRepository.finishById(aiTask.getId(), aiTask.getCreatedAt(), updatableAiTask, execInfo);
            });
        }
    }


    /**
     *  取消任务
     * @param taskId                任务ID
     * @param reason                取消原因
     * @param updatableAiTask       任务更新数据
     * @param execInfo              任务执行数据
     */
    private void cancel(long taskId, String reason, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        String finalReason = StringUtils.defaultIfBlank(reason, "Unknown Cancel Reason");
        List<AiTaskSynchronization> synchronizations = AiTaskTransactionManager.getSynchronizations();
        if (synchronizations.isEmpty()) {
            aiTaskRepository.cancelById(taskId, finalReason, updatableAiTask, execInfo);
        } else {
            transactionTemplate.executeWithoutResult(status -> {
                AiTaskTransactionManager.getSynchronizations().forEach(AiTaskSynchronization::inCommit);
                aiTaskRepository.cancelById(taskId, finalReason, updatableAiTask, execInfo);
            });
        }
    }


    /**
     * 完结任务调度
     * @param aiTask            任务实例
     * @param updatableAiTask   任务更新数据
     * @param execInfo          任务执行数据
     * @param config            任务分片
     */
    private void complete(AiTask aiTask, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo, AiTaskConfig config) {
        List<AiTaskSynchronization> synchronizations = AiTaskTransactionManager.getSynchronizations();
        transactionTemplate.executeWithoutResult(status -> {
            if (!synchronizations.isEmpty()) {
                AiTaskTransactionManager.getSynchronizations().forEach(AiTaskSynchronization::inCommit);
            }
            aiTaskRepository.completeById(aiTask.getId(), updatableAiTask, execInfo);
            // TODO 任务完成通知 : 后续根据 AiTask 的 taskType 和 bizType 决定是否发送通知 , 目前仅做简单日志
            if (config.isNotifyUserOnCompleted()) {
                appAlertHelper.alertText("[AiTask] taskName = {} , is completed !", aiTask.getTaskName());
            }
        });
    }

}
