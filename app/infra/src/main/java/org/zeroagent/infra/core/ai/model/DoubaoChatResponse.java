package org.zeroagent.infra.core.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  20时10分
 * @Description: 流式响应体
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoubaoChatResponse {
    private String id;
    private List<Choice> choices;
    private String model;
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Delta delta;
        private Integer index;
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        private String role;
        private String content;
        @JsonProperty("reasoning_content")
        private String reasoningContent;
        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolCall {
        private Integer index;
        private String id;
        private String type;
        private Function function;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Function {
        private String name;
        private String arguments;
    }

}
