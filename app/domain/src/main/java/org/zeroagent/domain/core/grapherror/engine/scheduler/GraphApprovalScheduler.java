package org.zeroagent.domain.core.grapherror.engine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.zeroagent.domain.common.async.AsyncPools;
import org.zeroagent.domain.common.async.AsyncTemplate;
import org.zeroagent.domain.core.grapherror.engine.GraphApprovalTaskEngine;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLogStatus;
import org.zeroagent.domain.core.grapherror.service.GraphErrorLogRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图谱审批任务调度器
 * @author Nuk3m1
 * @version 2026年04月23日  15时28分
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.graph-approval.scheduler", value = "enabled", havingValue = "true")
public class GraphApprovalScheduler {
    /**
     * 每次捞取数量
     */
    private static final int LOAD_LIMIT = 10;

    private final GraphErrorLogRepository graphErrorLogRepository;
    private final AsyncTemplate           asyncTemplate;
    private final TransactionTemplate     transactionTemplate;
    private final GraphApprovalTaskEngine graphApprovalTaskEngine;

    /**
     * 捞取审批工单并提交审批任务
     */
    @Scheduled(initialDelay = 20, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void pollApprovalTask() {
        List<GraphErrorLog> tasks = transactionTemplate.execute(status -> {
            List<GraphErrorLog> fetchTasks = graphErrorLogRepository.fetchBatchByStatus(LOAD_LIMIT, GraphErrorLogStatus.CREATED);
            fetchTasks.forEach(task -> graphErrorLogRepository.updateStatusById(task.getId(), GraphErrorLogStatus.WAITING));
            return fetchTasks;
        });
        if (tasks != null && !tasks.isEmpty()) {
            asyncTemplate.execute(AsyncPools.GRAPH_APPROVAL_EXECUTE_POOL, () -> graphApprovalTaskEngine.execute(tasks));
        }
    }
}
