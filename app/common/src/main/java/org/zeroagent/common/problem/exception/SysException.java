package org.zeroagent.common.problem.exception;

import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.error.ErrorCode;

import java.io.Serial;

/**
 * 系统异常
 */
public class SysException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1464062936773582866L;
    public SysException(String message) {
        super(CommonErrorCode.UNSPECIFIED, message);
    }
    public SysException(Message messagbe) {
        super(CommonErrorCode.UNSPECIFIED, messagbe);
    }
    public SysException(Message messagbe, Throwable cause) {
        super(CommonErrorCode.UNSPECIFIED, messagbe, cause);
    }
    public SysException(@NotNull ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    public SysException(@NotNull ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    public SysException(@NotNull ErrorCode errorCode, Message message) {
        super(errorCode, message);
    }
    public SysException(@NotNull ErrorCode errorCode, Message message, Throwable cause) {
        super(errorCode, message, cause);
    }
    public SysException(@NotNull ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    public SysException(@NotNull ErrorCode errorCode) {
        super(errorCode);
    }
    public SysException(@NotNull ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

}
