package org.zeroagent.domain.core.aitask.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月26日  23时01分
 */
@Getter
@RequiredArgsConstructor
public enum AiTaskExecStatus {
    WAITING("待执行"),
    EXECUTING("执行锁定中"),
    SUSPENDED("挂起暂不执行"),
    COMPLETED("执行完成")

    ;
    private final String desc;
}
