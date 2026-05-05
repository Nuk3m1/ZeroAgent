package org.zeroagent.infra.core.ai.toolcalling;

import lombok.Data;
import lombok.experimental.Accessors;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;

import java.util.List;

/**
 * 工具调用执行结果，包含原始意图和执行后的业务结果。
 * @author Nuk3m1
 * @version 2026年04月23日  17时04分
 */
@Data
@Accessors(chain = true)
public class ToolCallingExecutionResult {
    private List<ToolCallingIntent> intents;
    private List<ToolCallingBizResult> results;
}
