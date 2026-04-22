package org.zeroagent.domain.core.ai.chat.toolcalling;


import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 工厂类 - 仅通过该类获得并操作 工具调用函数
 * @author Nuk3m1
 * @version 2026年04月14日  21时04分
 */
@Component
public class ToolCallingFactory {
    private final Map<String, ToolCallingExecutor> registry = new HashMap<>();

    public ToolCallingFactory(List<ToolCallingExecutor> executors) {
        executors.forEach(e -> registry.put(e.getToolType().getFunctionName(), e));
    }

    public ToolCallingExecutor getExecutor(ToolCallingEnum type) {
        return registry.get(type.getFunctionName());
    }
    public ToolCallingExecutor getExecutor(String functionName) {
        return registry.get(functionName);
    }

    public List<ObjectNode> getToolDefinitionNodes(ToolCallingEnum... tools) {
        List<ObjectNode> toolDefinitionNodes = new ArrayList<>();
        for (ToolCallingEnum tool : tools) {
            ToolCallingExecutor executor = registry.get(tool.getFunctionName());
            toolDefinitionNodes.add(executor.getToolDefinitionNode());
        }
        return toolDefinitionNodes;
    }

}
