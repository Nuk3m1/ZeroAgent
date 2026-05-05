package org.zeroagent.infra.core.ai.toolcalling;

import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 公共工具调用执行模板：
 * 1. 流式聚合 tool-call 片段
 * 2. 执行本地工具
 * 3. 返回结构化执行结果
 * @author Nuk3m1
 * @version 2026年04月23日  17时05分
 */
public interface ToolCallingExecutionTemplate {
    Mono<ToolCallingExecutionResult> execute(LlmRequest llmRequest, String emptyToolCallErrorMessage);

    List<ToolCallingBizResult> executeToolCalling(List<ToolCallingIntent> intents);
}
