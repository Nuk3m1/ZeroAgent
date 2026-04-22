package org.zeroagent.domain.core.ai.chat.model.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenhua
 * @version 2026年03月05日  16时40分
 * @Description: AI对话 通用返回体
 */
@Data
@Accessors(chain = true)
public class MessageChunk {

    private Boolean isFinished;

    @JsonProperty("reasoning_content")
    private String  reasoningContent;

    private String  content;

    private String  role;

    private List<ToolCallFragment> toolCallFragments = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class ToolCallFragment {
        /**
         * 索引号
         */
        private Integer index;
        /**
         * 工具调用ID
         */
        private String toolCallId;
        /**
         * 工具名
         */
        private String toolName;
        /**
         * 工具碎片
         */
        private String toolArgumentsFragment;
    }

    public static MessageChunk from(LlmResponse response) {
        if (response.isEmpty()) {
            return new MessageChunk().setIsFinished(false).setContent("").setReasoningContent("");
        }
        MessageChunk messageChunk = new MessageChunk()
                .setContent(response.getContent())
                .setReasoningContent(response.getReasoningContent())
                .setIsFinished(response.isFinished());
        for (LlmResponse.ToolCallInfo toolCallInfo : response.getToolCalls()) {
            ToolCallFragment toolCallFragment = new ToolCallFragment()
                    .setIndex(toolCallInfo.getIndex())
                    .setToolCallId(toolCallInfo.getId())
                    .setToolName(toolCallInfo.getFunctionName())
                    .setToolArgumentsFragment(toolCallInfo.getFunctionArguments());
            messageChunk.getToolCallFragments().add(toolCallFragment);
        }
        return messageChunk;
    }
    public static MessageChunk done() {
        return new MessageChunk()
                .setIsFinished(true)
                .setContent("")
                .setReasoningContent("");
    }

}
