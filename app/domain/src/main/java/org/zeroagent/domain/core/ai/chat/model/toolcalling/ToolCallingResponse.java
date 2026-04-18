package org.zeroagent.domain.core.ai.chat.model.toolcalling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 工具调用 大模型输出实体类
 * @author Nuk3m1
 * @version 2026年04月14日  23时17分
 */
@Data
public class ToolCallingResponse {
    private String id;
    private List<Choice> choices;
    private String model;

    @Data
    public static class Choice {
        private long index;
        @JsonProperty("finish_reason")
        private String finishReason;

        private List<Message> messages;
    }
    @Data
    public static class Message {
        private String role;
        private String content;
        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;
    }
    @Data
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;
    }
    @Data
    public static class Function {
        private String name;
        private String arguments;
    }
}
