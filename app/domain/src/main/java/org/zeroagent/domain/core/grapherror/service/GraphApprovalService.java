package org.zeroagent.domain.core.grapherror.service;

import org.zeroagent.domain.core.grapherror.model.GraphApprovalDecision;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;

/**
 * 审批服务
 * @author Nuk3m1
 * @version 2026年04月23日  14时23分
 */
public interface GraphApprovalService {
    /**
     * 对单条图谱关系候选进行审批
     * @param graphErrorLog 审批工单
     * @return 审批结论
     */
    GraphApprovalDecision approve(GraphErrorLog graphErrorLog);
}
