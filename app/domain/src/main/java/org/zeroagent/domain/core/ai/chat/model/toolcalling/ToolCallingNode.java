package org.zeroagent.domain.core.ai.chat.model.toolcalling;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 工具调用函数 - JSON节点 -> ObjectNode
 * @see com.fasterxml.jackson.databind.node.ObjectNode
 * @author Nuk3m1
 * @version 2026年04月14日  20时49分
 */
@Data
@Accessors(chain = true)
public class ToolCallingNode {
    private String type = "function";

    private FunctionNode function;

    @Data
    @Accessors(chain = true)
    public static class FunctionNode {
        private String name;

        private String description;

        private ObjectNode parameters;
    }
}
