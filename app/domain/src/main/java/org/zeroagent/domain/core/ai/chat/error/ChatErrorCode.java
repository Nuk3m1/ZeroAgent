package org.zeroagent.domain.core.ai.chat.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.problem.error.ErrorCode;

/**
 * AI对话错误码
 * @author Nuk3m1
 * @version 2026年03月10日  17时01分
 */
@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    CHAT_CALL_LLM_ERROR("服务器繁忙，请稍后重试")
    ;
    private final String msg;

    @Override
    public String getCode() {
        return this.name();
    }
}
