package org.zeroagent.infra.core.ai.toolcalling;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.domain.core.ai.chat.error.ChatErrorCode;
import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import org.zeroagent.domain.core.ai.chat.service.ToolCallingTemplate;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingExecutor;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具调用执行模板实现，封装通用流程：
 * 流式聚合 -> 工具执行 -> 结果封装。
 * @author Nuk3m1
 * @version 2026年04月23日  17时06分
 */
@Component
@RequiredArgsConstructor
public class ToolCallingExecutionTemplateImpl implements ToolCallingExecutionTemplate {
    private final ToolCallingTemplate toolCallingTemplate;
    private final ToolCallingFactory  toolCallingFactory;

    @Override
    public Mono<ToolCallingExecutionResult> execute(LlmRequest llmRequest, String emptyToolCallErrorMessage) {
        Map<Integer, ToolCallingIntent> intentMap = new HashMap<>();
        String errorMessage = StringUtils.hasText(emptyToolCallErrorMessage) ? emptyToolCallErrorMessage : "未发生工具调用";
        return toolCallingTemplate.toolCallingChat(llmRequest, intentMap)
                .then(Mono.fromCallable(() -> {
                    if (intentMap.isEmpty()) {
                        throw new BizException(ChatErrorCode.CHAT_CALL_LLM_ERROR, errorMessage);
                    }
                    List<ToolCallingIntent> intents = new ArrayList<>(intentMap.values());
                    List<ToolCallingBizResult> results = executeToolCalling(intents);
                    return new ToolCallingExecutionResult()
                            .setIntents(intents)
                            .setResults(results);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public List<ToolCallingBizResult> executeToolCalling(List<ToolCallingIntent> intents) {
        List<ToolCallingBizResult> results = new ArrayList<>();
        if (intents == null || intents.isEmpty()) {
            return results;
        }
        for (ToolCallingIntent intent : intents) {
            if (intent == null || !StringUtils.hasText(intent.getToolName())) {
                continue;
            }
            ToolCallingExecutor executor = toolCallingFactory.getExecutor(intent.getToolName());
            if (executor == null) {
                continue;
            }
            String dbResult = executor.execute(intent.getToolArgumentsJson());
            ToolCallingBizResult toolCallingBizResult = new ToolCallingBizResult();
            toolCallingBizResult.setToolCallId(intent.getToolCallId());
            toolCallingBizResult.setToolName(intent.getToolName());
            toolCallingBizResult.setToolArgumentsJson(intent.getToolArgumentsJson());
            toolCallingBizResult.setDbResult(dbResult);
            results.add(toolCallingBizResult);
        }
        return results;
    }
}
