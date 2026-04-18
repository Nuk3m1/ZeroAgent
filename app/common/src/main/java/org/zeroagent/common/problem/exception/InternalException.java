package org.zeroagent.common.problem.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.problem.error.ErrorCode;

import java.io.Serial;

/**
 * @author chenhua
 * @version 2026年03月04日  14时12分
 * @Description:
 */
public class InternalException extends Exception {
    @Serial
    private static final long serialVersionUID = -3864813014986836392L;

    @NotNull
    @Getter
    private final ErrorCode errorCode;

    /**
     * 构造方法
     *
     * @param errorCode 错误码
     * @param cause     根源异常
     */
    public InternalException(@NotNull ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.errorCode = errorCode;
    }
}
