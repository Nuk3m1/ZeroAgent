package org.zeroagent.common.problem.exception;

import lombok.Getter;
import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.problem.error.ErrorCode;
import org.zeroagent.common.utils.FormatUtil;

import java.io.Serial;


@Getter
public abstract class BaseException extends RuntimeException implements GeneralException {
    @Serial
    private static final long serialVersionUID = -7957254421739572750L;
    @NotNull
    private final ErrorCode errorCode;
    public BaseException(@NotNull ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
    public BaseException(@NotNull ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.errorCode = errorCode;
    }
    public BaseException(@NotNull ErrorCode errorCode, String message) {
        super(buildErrorMsg(errorCode, message));
        this.errorCode = errorCode;
    }
    public BaseException(@NotNull ErrorCode errorCode, String message, Throwable cause) {
        super(buildErrorMsg(errorCode, message), cause);
        this.errorCode = errorCode;
    }
    public BaseException(@NotNull ErrorCode errorCode, Message message) {
        super(message.getFormattedMessage());
        this.errorCode = errorCode;
    }
    public BaseException(@NotNull ErrorCode errorCode, Message message, Throwable cause) {
        super(message.getFormattedMessage(), cause);
        this.errorCode = errorCode;
    }
    public BaseException(@NotNull ErrorCode errorCode, Object... args) {
        super(FormatUtil.format(errorCode.getMsg(), args));
        if (args != null) {
            final int argCount = args.length;
            if (argCount > 0 && args[argCount - 1] instanceof Throwable) {
                super.initCause((Throwable) args[argCount - 1]);
            }
        }
        this.errorCode = errorCode;
    }

    private static String buildErrorMsg(ErrorCode errorCode, String message) {
        return FormatUtil.countArgumentPlaceholders(errorCode.getMsg()) == 1 ? FormatUtil.format(errorCode.getMsg(), message) : message;
    }
}
