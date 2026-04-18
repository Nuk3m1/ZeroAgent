package org.zeroagent.domain.core.ai.chat.model.toolcalling;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 大模型 - 工具调用意图
 * @author Nuk3m1
 * @version 2026年04月15日  21时04分
 */
@Data
@Accessors(chain = true)
public class ToolCallingIntent {
    private String toolCallId;
    private String toolName;
    private String toolArgumentsJson;
}
