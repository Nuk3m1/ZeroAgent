package org.zeroagent.domain.core.grapherror.engine;

import org.jetbrains.annotations.NotNull;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;

import java.util.List;

/**
 * 图谱审批任务执行引擎
 * @author Nuk3m1
 * @version 2026年04月23日  15时26分
 */
public interface GraphApprovalTaskEngine {
    /**
     * 执行审批任务批次
     * @param graphErrorLogs 审批工单批次
     */
    void execute(@NotNull List<GraphErrorLog> graphErrorLogs);
}
