package org.zeroagent.domain.core.ai.chat.model.toolcalling;

import lombok.Data;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月14日  22时32分
 */
@Data
public class ToolCallingBizResult {
    /**
     * 状态位 (是否需要工具调用)
     */
    private boolean isToolCall;

    /**
     * 普通对话内容
     */
    private String content;

    private String toolCallId; // 流水号
    private String toolName;   // 函数名
    private String toolArgumentsJson; // 大模型需要执行的参数，不是执行后的结果
    // 执行后的结果
    private String dbResult;


}
