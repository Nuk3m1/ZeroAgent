package org.zeroagent.infra.core.ai.toolcalling;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.common.reactive.ErrorMapper;
import org.zeroagent.domain.core.ai.chat.error.ChatErrorCode;
import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;
import org.zeroagent.domain.core.ai.chat.model.response.LlmResponse;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import org.zeroagent.domain.core.ai.chat.service.ToolCallingTemplate;
import org.zeroagent.infra.core.ai.model.DoubaoChatResponse;
import org.zeroagent.infra.integration.WebClientFactory;
import org.zeroagent.infra.integration.llm.doubao.DouBaoChatProperties;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 工具调用 对话服务 实现
 * @author Nuk3m1
 * @version 2026年04月23日  15时44分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallingTemplateImpl implements ToolCallingTemplate {
    private final ObjectMapper                  objectMapper;
    private final WebClientFactory              webClientFactory;
    private final DouBaoChatProperties          douBaoChatProperties;

    @Override
    public Flux<MessageChunk> toolCallingChat(LlmRequest llmRequest, Map<Integer, ToolCallingIntent> intentMap) {
        return webClientFactory.doubaoChatWebClient()
                .post()
                .uri(douBaoChatProperties.getCompletionPath())
                .bodyValue(objectMapper.valueToTree(llmRequest))
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorMap(ErrorMapper.sysException(ChatErrorCode.CHAT_CALL_LLM_ERROR))
                .doOnError(err -> log.error("关系提取异常", err))
                .filter(StringUtils::hasText)
                .map(line -> line.replaceFirst("^data: ", "").trim())
                .map(this::toMessageChunk)
                .doOnNext(chunk -> {
                    if (chunk.getToolCallFragments() != null && !chunk.getToolCallFragments().isEmpty()) {
                        for (MessageChunk.ToolCallFragment toolCallFragment : chunk.getToolCallFragments()) {
                            Integer index = toolCallFragment.getIndex();
                            ToolCallingIntent intent = intentMap.computeIfAbsent(index, key -> new ToolCallingIntent());
                            if (StringUtils.hasLength(toolCallFragment.getToolCallId())) {
                                intent.setToolCallId(toolCallFragment.getToolCallId());
                            }
                            if (StringUtils.hasLength(toolCallFragment.getToolName())) {
                                intent.setToolName(toolCallFragment.getToolName());
                            }
                            if (StringUtils.hasLength(toolCallFragment.getToolArgumentsFragment())) {
                                String currentArgs = intent.getToolArgumentsJson() == null ? "" : intent.getToolArgumentsJson();
                                intent.setToolArgumentsJson(currentArgs + toolCallFragment.getToolArgumentsFragment());
                            }
                        }
                    }
                });
    }

    /**
     *  String 反序列化为 MessageChunk
     * @param data String类型响应体
     * @return MessageChunk模型
     */
    private MessageChunk toMessageChunk(String data) {
//        log.info(data);
        if ("[DONE]".equals(data)) {
            return MessageChunk.done();
        }
        try {
            LlmResponse response = objectMapper.readValue(data, DoubaoChatResponse.class);
//            log.info(response.toString());
            return MessageChunk.from(response);
        } catch (Exception e) {
            log.error("解析DouBaoChatAPI Response异常: {}", data, e);
            // 根据业务需求，这里可以选择抛出自定义异常，或者返回包含错误信息的 Chunk
            throw new RuntimeException("流解析失败", e);
        }
    }
}
