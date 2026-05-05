package org.zeroagent.api.config.security.error;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import org.zeroagent.common.problem.error.ErrorCode;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月27日  19时39分
 */
@Getter
public class BizAuthenticationException extends AuthenticationException {
    private final ErrorCode errorCode;


    public BizAuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
    public BizAuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.errorCode = errorCode;
    }
}
