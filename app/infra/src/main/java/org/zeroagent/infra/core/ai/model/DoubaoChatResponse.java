package org.zeroagent.infra.core.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.core.ai.chat.model.response.LlmResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  20时10分
 * @Description: 流式响应体
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoubaoChatResponse implements LlmResponse {


    private String id;
    private List<Choice> choices;
    private String model;

    // ---------------- 内部映射静态类 --------------------------------
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


    @Override
    public boolean isEmpty() {
        return choices == null || choices.isEmpty();
    }

    @Override
    public String getContent() {
       if (isEmpty() || choices.getFirst().getDelta() == null) {
           return "";
       }
       String content = choices.getFirst().getDelta().getContent();
       return content != null ? content : "";
    }

    @Override
    public String getReasoningContent() {
        if (isEmpty() || choices.getFirst().getDelta() == null) {
            return "";
        }
        String reasoningContent = choices.getFirst().getDelta().getReasoningContent();
        return reasoningContent != null ? reasoningContent : "";
    }

    @Override
    public boolean isFinished() {
        if (isEmpty()) {
            // 心跳包的情况下可能为空，但是此时流没有结束
            return false;
        }
        return StringUtils.hasText(choices.getFirst().getFinishReason());
    }

    @Override
    public List<ToolCallInfo> getToolCalls() {
        if (isEmpty() || choices.getFirst().getDelta() == null || choices.getFirst().getDelta().getToolCalls() == null) {
            return Collections.emptyList();
        }
        return choices.getFirst().getDelta().getToolCalls().stream()
                .map(tc -> new ToolCallInfo() {
                    @Override
                    public Integer getIndex() {
                        return tc.getIndex();
                    }

                    @Override
                    public String getId() {
                        return tc.getId();
                    }

                    @Override
                    public String getFunctionName() {
                        return tc.getFunction() != null ? tc.getFunction().getName() : null;
                    }

                    @Override
                    public String getFunctionArguments() {
                        return tc.getFunction() != null ? tc.getFunction().getArguments() : null;
                    }
                }).collect(Collectors.toList());

    }

}
