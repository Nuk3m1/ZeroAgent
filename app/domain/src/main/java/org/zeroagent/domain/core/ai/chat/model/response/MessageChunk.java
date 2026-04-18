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

    private Boolean finished;

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

}
