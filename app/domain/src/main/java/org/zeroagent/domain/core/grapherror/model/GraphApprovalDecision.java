package org.zeroagent.domain.core.grapherror.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 审批结果
 * @author Nuk3m1
 * @version 2026年04月23日  14时22分
 */
@Data
@Accessors(chain = true)
public class GraphApprovalDecision {
    /**
     * 是否审批通过
     */
    private boolean approved;
    /**
     * 审批备注/拒绝原因
     */
    private String  reason;
}
