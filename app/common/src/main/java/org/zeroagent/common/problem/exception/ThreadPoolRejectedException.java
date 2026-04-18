package org.zeroagent.common.problem.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.task.TaskRejectedException;
import org.zeroagent.common.problem.error.ErrorCode;

import java.io.Serial;


@Getter
public class ThreadPoolRejectedException extends TaskRejectedException implements GeneralException {
    @Serial
    private static final long serialVersionUID = 5417471669003569794L;

    @NotNull
    private final ErrorCode errorCode;

    /**
     * 构造方法
     *
     * @param errorCode 错误码
     */
    public ThreadPoolRejectedException(@NotNull ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
