package org.zeroagent.domain.common.integration.feishu.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.problem.error.ErrorCode;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月17日  15时46分
 */
@Getter
@RequiredArgsConstructor
public enum FeiShuErrorCode implements ErrorCode {
    CARD_SEND_ERROR("[飞书] 卡片请求发送失败"),
    GET_TOKEN_FAILED("[飞书] Token 获取失败")
    ;
    private final String msg;

    @Override
    public String getCode() {
        return this.name();
    }
}
