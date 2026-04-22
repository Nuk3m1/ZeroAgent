package org.zeroagent.domain.core.ai.chat.model.response;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月21日  21时11分
 */
public interface LlmResponse {
    /**
     * 判断响应包是否为空 (如心跳包)
     */
    boolean isEmpty();


    /**
     * 获取正文内容
     */
    String getContent();

    /**
     * 获取思考链内容
     */
    String getReasoningContent();

    /**
     * 判断当前流是否已经结束
     */
    boolean isFinished();

    /**
     * 获取工具调用列表
     */
    List<ToolCallInfo> getToolCalls();
    interface ToolCallInfo {
        Integer getIndex();
        String getId();
        String getFunctionName();
        String getFunctionArguments();
    }
}
