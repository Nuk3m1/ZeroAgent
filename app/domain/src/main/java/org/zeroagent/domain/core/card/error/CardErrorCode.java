package org.zeroagent.domain.core.card.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.problem.error.ErrorCode;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月20日  17时50分
 */
@Getter
@RequiredArgsConstructor
public enum CardErrorCode implements ErrorCode {
    ARCHETYPE_NOT_EXISTS("字段不存在"),
    HTTP_ERROR("网络请求异常")


    ;
    private final String desc;
    @Override
    public String getMsg() {
        return desc;
    }
    @Override
    public String getCode() {
        return this.name();
    }
}
