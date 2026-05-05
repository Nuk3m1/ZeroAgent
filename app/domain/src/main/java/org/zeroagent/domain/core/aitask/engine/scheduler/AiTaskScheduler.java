package org.zeroagent.domain.core.aitask.engine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.zeroagent.domain.common.async.AsyncPools;
import org.zeroagent.domain.common.async.AsyncTemplate;
import org.zeroagent.domain.core.aitask.engine.AiTaskEngine;
import org.zeroagent.domain.core.aitask.engine.handler.factory.AiTaskHandlerFactory;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskConfig;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskExecStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;
import org.zeroagent.domain.core.aitask.service.AiTaskRepository;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AiTask 核心调度器
 * @author Nuk3m1
 * @version 2026年05月03日  02时09分
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.ai-task.scheduler", value = "enabled", havingValue = "true")
public class AiTaskScheduler {
    private final AiTaskEngine         aiTaskEngine;
    private final AiTaskHandlerFactory aiTaskHandlerFactory;
    private final AiTaskRepository     aiTaskRepository;
    private final AppAlertHelper       appAlertHelper;
    private final AsyncTemplate        asyncTemplate;
    private final TransactionTemplate transactionTemplate;


    /**
     * 仅作为触发器 ， 把控 loader 捞取 及 状态机流转全流程, 具体逻辑
     * 考虑loader层面走DB，MQ多种实现方式
     */
    @Scheduled(initialDelay = 10, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void pollAiTasks() {
        Set<AiTaskConfig> configs = this.entry();
        if (configs.isEmpty()) {
            log.warn("[AiTaskScheduler]no task configs are set");
            return;
        }
        if (!asyncTemplate.checkExecutorQueueCapacity(AsyncPools.AI_TASK_EXECUTE_POOL)) {
            return;
        }


        for (AiTaskConfig config : configs) {
            try {
                List<AiTask> aiTasks = this.load(config);
                if (aiTasks.isEmpty()) {
                    continue;
                }
                this.execute(aiTasks);
            } catch (Exception e) {
                log.error("[AiTaskScheduler]task scheduler error, taskConfig = {}", config);
            }
        }

    }



    /**
     * 获取可执行分片
     * TODO 在这里做 黑白名单过滤 及 环境隔离
     * @return 分片列表
     */
    private Set<AiTaskConfig> entry() {
        return aiTaskHandlerFactory.getAllTaskConfigs();
    }


    /**
     * 并行捞取 可执行 AiTask
     * @param taskConfig 任务分片
     * @return            待执行的任务列表
     */
    private List<AiTask> load(AiTaskConfig taskConfig) {
        List<AiTask> aiTasksLoad = new ArrayList<>();
        try {
            AiTaskType taskType = taskConfig.getTaskType();
            String bizType = taskConfig.getBizType();
            Integer loadSize = taskConfig.getLoadSize();

            long start = System.currentTimeMillis();
            List<AiTask> aiTasks = aiTaskRepository.queryWaitingForAutoExec(taskType, bizType, loadSize);
            long costMills = System.currentTimeMillis() - start;

            log.debug("[AiTaskScheduler][load] taskType = {}, bizType = {}, loadSize = {}, actualSize = {}, cost = {}ms",
                    taskType, bizType, loadSize, aiTasks.size(), costMills);

            if (!aiTasks.isEmpty()) {
                aiTasksLoad.addAll(aiTasks);
            }
        } catch (Exception e) {
            appAlertHelper.alertText("[AiTaskScheduler][load][Exception] ", e);
            return Collections.emptyList();
        }
        return aiTasksLoad;
    }


    private void execute(List<AiTask> aiTasks) {
        if (aiTasks == null || aiTasks.isEmpty()) {
            log.debug("[AiTaskScheduler][execute] No tasks to execute");
            return;
        }

        for (AiTask aiTask : aiTasks) {
            transactionTemplate.executeWithoutResult(status -> {
                aiTaskRepository.updateExecuteStatusById(aiTask.getId(), AiTaskExecStatus.EXECUTING);
            });
            asyncTemplate.execute(AsyncPools.AI_TASK_EXECUTE_POOL, () -> {
                log.debug("[AiTaskScheduler][execute] taskId = {}, taskType = {}, bizType = {}]",
                        aiTask.getId(), aiTask.getBizType(), aiTask.getBizType());
                long start = System.currentTimeMillis();
                try {
                    aiTaskEngine.execute(aiTask);
                } catch (Exception e) {
                    log.error("[AiTaskScheduler][execute][Exception] AiTaskEngine execute error", e);
                    aiTaskRepository.updateExecuteStatusById(aiTask.getId(), AiTaskExecStatus.WAITING);
                }
                long end = System.currentTimeMillis();
                log.debug("[AiTaskScheduler][execute][endReport] taskId = {}, taskType = {}. bizType = {}, cost = {}ms",
                        aiTask.getId(), aiTask.getBizType(), aiTask.getBizType(), end - start);
            });
        }

    }


}
