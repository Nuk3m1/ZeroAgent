package org.zeroagent.domain.core.ai.chat.toolcalling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 所有工具方法必须实现该接口，符合其规范
 * @author Nuk3m1
 * @version 2026年04月14日  20时35分
 */
public interface ToolCallingExecutor {
    /**
     * 工具函数名
     * @return 工具函数名
     */
    ToolCallingEnum getToolType();

    /**
     * 构造工具的JSON节点
     * @return 工具节点对象
     */
    ObjectNode getToolDefinitionNode();

    /**
     * 工具执行逻辑
     * @return 工具执行结果 -- 应注册为 DbResult
     */
    String execute(String arguments);

}
