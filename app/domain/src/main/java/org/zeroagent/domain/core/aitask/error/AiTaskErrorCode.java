package org.zeroagent.domain.core.aitask.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.problem.error.ErrorCode;

/**
 *
 * @author Nuk3m1
 * @version 2026年05月02日  17时54分
 */
@Getter
@RequiredArgsConstructor
public enum AiTaskErrorCode implements ErrorCode {
    TASK_NOT_EXISTS("任务不存在"),
    TASK_SUSPEND_FAIL("任务挂起失败"),
    TASK_IS_LOCKING_FOR_EXECUTION("任务正在执行中"),
    TASK_IS_NOT_SUSPENDED("任务没有被挂起"),
    TASK_RESUME_FAIL("任务挂起恢复失败"),
    ;


    private final String msg;
    @Override
    public String getCode() {
        return this.name();
    }


}
