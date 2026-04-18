package org.zeroagent.common.problem.exception;

import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.problem.error.ErrorCode;

import java.io.Serial;

/**
 * 业务异常
 */
public class BizException extends BaseException {
    @Serial
    private static final long serialVersionUID = -5895794127176527837L;

    public BizException(@NotNull ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(@NotNull ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BizException(@NotNull ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BizException(@NotNull ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BizException(@NotNull ErrorCode errorCode, Message message) {
        super(errorCode, message);
    }

    public BizException(@NotNull ErrorCode errorCode, Message message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BizException(@NotNull ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
