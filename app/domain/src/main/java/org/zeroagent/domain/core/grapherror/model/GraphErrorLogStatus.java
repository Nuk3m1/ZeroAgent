package org.zeroagent.domain.core.grapherror.model;

import lombok.Getter;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月21日  23时13分
 */
@Getter
public enum GraphErrorLogStatus {
    CREATED("待审核", 1),
    WAITING("待调度", 2),
    SUCCESS("审核完成", 3),
    FAILED("失败", 0)
    ;
    private final String description;
    private final int status;
    GraphErrorLogStatus(String description, int status) {
        this.description = description;
        this.status = status;
    }

}
