package org.zeroagent.domain.core.user.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.problem.error.ErrorCode;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月27日  19时41分
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_LOGIN("用户未登录"),

    USERNAME_ALREADY_EXISTS("用户名: {} 已存在"),
    USER_EMAIL_ALREADY_EXISTS("用户邮箱: {} 已存在"),

    USER_NOT_EXISTS("用户不存在"),

    ;
    private final String desc;

    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public String getMsg() {
        return this.desc;
    }
}
