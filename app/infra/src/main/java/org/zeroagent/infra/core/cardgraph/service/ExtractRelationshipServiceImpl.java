package org.zeroagent.infra.core.cardgraph.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.common.reactive.ErrorMapper;
import org.zeroagent.domain.core.ai.chat.error.ChatErrorCode;
import org.zeroagent.domain.core.ai.chat.model.SystemPromptPool;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;
import org.zeroagent.domain.core.ai.chat.model.message.*;
import org.zeroagent.domain.core.ai.chat.model.response.LlmResponse;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingFactory;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingService;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLogStatus;
import org.zeroagent.domain.core.grapherror.service.ExtractRelationshipService;
import org.zeroagent.domain.core.grapherror.service.GraphErrorLogRepository;
import org.zeroagent.infra.core.ai.model.DouBaoChatRequest;
import org.zeroagent.infra.core.ai.model.DoubaoChatResponse;
import org.zeroagent.infra.integration.WebClientFactory;
import org.zeroagent.infra.integration.llm.doubao.DouBaoChatProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月21日  20时48分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractRelationshipServiceImpl implements ExtractRelationshipService {
    private final TransactionTemplate           transactionTemplate;
    private final ToolCallingService            toolCallingService;
    private final WebClientFactory              webClientFactory;
    private final ObjectMapper                  objectMapper;
    private final DouBaoChatProperties          douBaoChatProperties;
    private final ToolCallingFactory            toolCallingFactory;
    private final CardInformationRepository     cardInformationRepository;
    private final GraphErrorLogRepository       graphErrorLogRepository;


    @Override
    public void extractCardRules(String sourceCardId,String sourceCardName, String effect, ToolCallingEnum toolCallingEnum) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(MediaType.TEXT, SystemPromptPool.CARD_SEARCH_RULES_EXTRACT_PROMPT));
        messages.add(new UserMessage(MediaType.TEXT, sourceCardName + ":\n" + effect));
        DouBaoChatRequest requestBody = new DouBaoChatRequest()
                .setModel(douBaoChatProperties.getModel())
                .setMessages(messages);
        if (requestBody.getTools() == null || requestBody.getTools().isEmpty()) {
            List<ObjectNode> allTools = toolCallingFactory.getToolDefinitionNodes(toolCallingEnum);
            requestBody.setTools(allTools);
        }
        Map<Integer, ToolCallingIntent> intentMap = new HashMap<>();
        webClientFactory.doubaoChatWebClient()
                .post()
                .uri(douBaoChatProperties.getCompletionPath())
                .bodyValue(objectMapper.valueToTree(requestBody))
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
                })
                .then(Mono.defer(() -> {
                    if (intentMap.isEmpty()) {
                        return Mono.error(new BizException(ChatErrorCode.CHAT_CALL_LLM_ERROR, "提取图谱关系时未发生工具调用"));
                    }
                    List<ToolCallingIntent> intents = new ArrayList<>(intentMap.values());
                    List<ToolCallingBizResult> results = toolCallingService.executeToolCalling(intents);
                    try {
                        transactionTemplate.executeWithoutResult(status -> {
                            for (ToolCallingBizResult result : results) {
                                try {
                                    List<Long> cardIds = objectMapper.readValue(result.getDbResult(), new TypeReference<List<Long>>() {});
                                    for (Long cardId : cardIds) {
                                        // 通过卡密搜索目标卡牌
                                        Optional<CardInformation> cardInfoOpt = cardInformationRepository.findByPassCode(String.valueOf(cardId));
                                        String targetCardName = cardInfoOpt.isPresent() ? cardInfoOpt.get().getName() : "";
                                        GraphErrorLog graphErrorLog = new GraphErrorLog()
                                                .setId(IdHelper.getId())
                                                // 源卡牌卡密
                                                .setSourceCardId(Long.valueOf(sourceCardId))
                                                .setSourceCardName(sourceCardName)
                                                .setLlmRawResponse(JSON.toJSONObject(intents.getFirst()))
                                                .setErrorMessage(targetCardName.isEmpty() ? "未找到对应卡牌记录" : "SUCCESS")
                                                .setErrorType("[NONE]")
                                                .setStatus(GraphErrorLogStatus.CREATED)
                                                // 目标卡牌卡密
                                                .setTargetCardId(cardId)
                                                .setTargetCardName(targetCardName);
                                        graphErrorLogRepository.create(graphErrorLog);
                                    }
                                } catch (JsonProcessingException e) {
                                    log.error("工具调用结果 解析过程出错", e);
                                    throw new BizException(ChatErrorCode.CHAT_CALL_LLM_ERROR, "工具调用结果 解析过程出错", e);
                                }
                            }
                        });
                        return Mono.empty();
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(unused -> {}, // 数据在流处理中已经被消费，这里忽略
                        err -> log.error("提取卡牌规则流执行彻底失败", err),
                        () -> log.info("提取卡牌规则流执行完毕，日志入库请及时处理"));
    }

    /**
     *  String 反序列化为 MessageChunk
     * @param data String类型响应体
     * @return MessageChunk模型
     */
    private MessageChunk toMessageChunk(String data) {
        log.info(data);
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
