package org.zeroagent.domain.core.ai.chat.model.toolcalling;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<Map<String, Object>> formatIntentsForLlm(List<ToolCallingIntent> intents) {
        return intents.stream().map(intent -> {
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("id", intent.getToolCallId());
            toolCall.put("type", "function");

            Map<String, String> function = new HashMap<>();
            function.put("name", intent.getToolName());
            function.put("arguments", intent.getToolArgumentsJson());
            toolCall.put("function", function);
            return toolCall;
        }).toList();
    }
}
