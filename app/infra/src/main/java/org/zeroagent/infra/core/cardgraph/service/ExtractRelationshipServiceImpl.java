package org.zeroagent.infra.core.cardgraph.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.zeroagent.domain.core.ai.chat.error.ChatErrorCode;
import org.zeroagent.domain.core.ai.chat.model.SystemPromptPool;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;
import org.zeroagent.domain.core.ai.chat.model.message.Message;
import org.zeroagent.domain.core.ai.chat.model.message.SystemMessage;
import org.zeroagent.domain.core.ai.chat.model.message.UserMessage;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingFactory;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.cardgraph.model.GraphRelationTypeEnum;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLogStatus;
import org.zeroagent.domain.core.grapherror.service.ExtractRelationshipService;
import org.zeroagent.domain.core.grapherror.service.GraphErrorLogRepository;
import org.zeroagent.infra.core.ai.model.DouBaoChatRequest;
import org.zeroagent.infra.core.ai.toolcalling.ToolCallingExecutionTemplate;
import org.zeroagent.infra.integration.llm.doubao.DouBaoChatProperties;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SEARCH 关系抽取服务。
 * @author Nuk3m1
 * @version 2026年04月21日  20时48分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractRelationshipServiceImpl implements ExtractRelationshipService {
    private static final List<String> SEARCH_INCLUDE_KEYWORDS = List.of(
            "加入手卡", "加入到手卡", "加入手牌", "加入到手牌", "加到手牌", "加入手中", "加入到手中"
    );

    private static final List<String> SEARCH_EXCLUDE_KEYWORDS = List.of(
            "特殊召唤", "从墓地加入手卡", "从墓地加入到手卡", "从墓地加入手牌", "从墓地加入到手牌",
            "从墓地特殊召唤", "墓地回收", "除外", "送墓", "送去墓地", "破坏", "回到卡组", "回到牌组", "返回卡组", "返回牌组"
    );

    private final TransactionTemplate transactionTemplate;
    private final ToolCallingExecutionTemplate toolCallingExecutionTemplate;
    private final ObjectMapper objectMapper;
    private final DouBaoChatProperties douBaoChatProperties;
    private final ToolCallingFactory toolCallingFactory;
    private final CardInformationRepository cardInformationRepository;
    private final GraphErrorLogRepository graphErrorLogRepository;

    @Override
    public Mono<Void> extractCardRules(String sourceCardPassCode,
                                       String sourceCardName,
                                       String effect,
                                       ToolCallingEnum toolCallingEnum,
                                       GraphRelationTypeEnum graphRelationTypeEnum) {
        if (toolCallingEnum == null || graphRelationTypeEnum == null) {
            throw new BizException(ChatErrorCode.PARAM_ERROR);
        }
        // 执行语句预过滤
        SearchSegmentFilterResult segmentFilterResult = filterSearchSegments(effect);
        if (segmentFilterResult.keptSegments().isEmpty()) {
            log.info("[语义抽取] 卡牌 {}({}) 无 SEARCH 有效句段，直接跳过抽取。rawSegments={}, droppedSegments={}",
                    sourceCardName,
                    sourceCardPassCode,
                    segmentFilterResult.rawSegmentCount(),
                    segmentFilterResult.droppedSegments().size());
            return Mono.empty();
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(MediaType.TEXT, SystemPromptPool.CARD_SEARCH_RULES_EXTRACT_PROMPT));
        messages.add(new UserMessage(MediaType.TEXT, sourceCardName + ":\n" + String.join("\n", segmentFilterResult.keptSegments())));

        DouBaoChatRequest requestBody = new DouBaoChatRequest()
                .setModel(douBaoChatProperties.getModel())
                .setMessages(messages);

        if (requestBody.getTools() == null || requestBody.getTools().isEmpty()) {
            List<ObjectNode> allTools = toolCallingFactory.getToolDefinitionNodes(toolCallingEnum);
            requestBody.setTools(allTools);
        }

        return toolCallingExecutionTemplate.execute(requestBody, "提取图谱关系时未发生工具调用")
                .flatMap(executionResult -> {
                    List<ToolCallingIntent> intents = executionResult.getIntents();
                    List<ToolCallingBizResult> results = executionResult.getResults();
                    if (results == null || results.isEmpty()) {
                        return Mono.<Void>error(new BizException(ChatErrorCode.CHAT_CALL_LLM_ERROR, "工具调用执行结果为空"));
                    }

                    try {
                        ToolExecutionAggregate aggregate = aggregateToolExecutionResult(results);
                        log.info("[语义抽取统计] sourceCardPassCode={}, rawSegments={}, keptSegments={}, droppedSegments={}, rawGroups={}, validGroups={}, droppedGroups={}, targets={}",
                                sourceCardPassCode,
                                segmentFilterResult.rawSegmentCount(),
                                segmentFilterResult.keptSegments().size(),
                                segmentFilterResult.droppedSegments().size(),
                                aggregate.rawGroupCount(),
                                aggregate.validGroupCount(),
                                aggregate.droppedGroupCount(),
                                aggregate.targetCardPassCodes().size());

                        if (aggregate.targetCardPassCodes().isEmpty()) {
                            log.info("[语义抽取] 卡牌 {}({}) 最终无有效目标卡，不创建审批工单。", sourceCardName, sourceCardPassCode);
                            return Mono.empty();
                        }

                        Map<String, Object> llmRawResponse = buildLlmRawResponse(intents, segmentFilterResult, aggregate);

                        transactionTemplate.executeWithoutResult(status -> {
                            Long sourceCardId = parsePassCode(sourceCardPassCode);
                            if (sourceCardId == null) {
                                throw new BizException(ChatErrorCode.PARAM_ERROR, "sourceCardPassCode 非法: " + sourceCardPassCode);
                            }

                            for (String targetCardPassCode : aggregate.targetCardPassCodes()) {
                                Long targetCardId = parsePassCode(targetCardPassCode);
                                if (targetCardId == null) {
                                    log.warn("目标卡密非法，跳过工单创建: {}", targetCardPassCode);
                                    continue;
                                }

                                Optional<CardInformation> cardInfoOpt = cardInformationRepository.findByPassCode(targetCardPassCode);
                                String targetCardName = cardInfoOpt.map(CardInformation::getName).orElse("");
                                String targetCardEffect = cardInfoOpt.map(CardInformation::getEffect).orElse("");

                                GraphErrorLog graphErrorLog = new GraphErrorLog()
                                        .setId(IdHelper.getId())
                                        .setSourceCardId(sourceCardId)
                                        .setSourceCardName(sourceCardName)
                                        .setLlmRawResponse(JSON.toJSONObject(llmRawResponse))
                                        .setErrorMessage(targetCardName.isEmpty() ? "未找到对应卡牌记录" : "[CREATED]")
                                        .setErrorType("[CREATED]")
                                        .setStatus(GraphErrorLogStatus.CREATED)
                                        .setTargetCardId(targetCardId)
                                        .setTargetCardName(targetCardName)
                                        .setGraphRelationType(graphRelationTypeEnum.name())
                                        .setSourceCardEffect(effect)
                                        .setTargetCardEffect(targetCardEffect);
                                graphErrorLogRepository.create(graphErrorLog);
                            }
                        });
                        return Mono.<Void>empty();
                    } catch (Exception e) {
                        return Mono.<Void>error(e);
                    }
                })
                .doOnSuccess(unused -> log.info("提取卡牌规则流执行完毕，日志入库请及时处理"))
                .doOnError(err -> log.error("提取卡牌规则流执行彻底失败", err));
    }

    private SearchSegmentFilterResult filterSearchSegments(String effect) {
        if (!StringUtils.hasText(effect)) {
            return new SearchSegmentFilterResult(0, List.of(), List.of());
        }

        List<String> segments = splitEffectSegments(effect);
        if (segments.isEmpty()) {
            return new SearchSegmentFilterResult(0, List.of(), List.of());
        }

        List<String> kept = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        for (String segment : segments) {
            if (isSearchSegment(segment)) {
                kept.add(segment);
            } else {
                dropped.add(segment);
            }
        }

        return new SearchSegmentFilterResult(segments.size(), kept, dropped);
    }

    private List<String> splitEffectSegments(String effect) {
        String normalized = effect.replace("\r\n", "\n").replace('\r', '\n');
        normalized = normalized.replaceAll("(?=●\\d+[：:])", "\n");
        normalized = normalized.replaceAll("(?=[①②③④⑤⑥⑦⑧⑨⑩])", "\n");
        normalized = normalized.replaceAll("(?=\\d+[\\.、])", "\n");

        return Arrays.stream(normalized.split("\\n+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private boolean isSearchSegment(String segment) {
        String normalized = normalizeForCompare(segment);
        if (!normalized.contains("从卡组")) {
            return false;
        }
        if (!containsAny(normalized, SEARCH_INCLUDE_KEYWORDS)) {
            return false;
        }
        return !containsAny(normalized, SEARCH_EXCLUDE_KEYWORDS);
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeForCompare(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.replace(" ", "")
                .replace("\t", "")
                .replace("　", "");
    }

    private ToolExecutionAggregate aggregateToolExecutionResult(List<ToolCallingBizResult> results) throws JsonProcessingException {
        LinkedHashSet<String> targetCardPassCodes = new LinkedHashSet<>();
        List<Map<String, Object>> validConditionGroups = new ArrayList<>();
        int rawGroupCount = 0;
        int validGroupCount = 0;
        int droppedGroupCount = 0;

        for (ToolCallingBizResult result : results) {
            SearchToolExecutionResult executionResult = parseSearchToolExecutionResult(result.getDbResult());
            targetCardPassCodes.addAll(executionResult.targetCardPassCodes());
            validConditionGroups.addAll(executionResult.validConditionGroups());
            rawGroupCount += executionResult.rawGroupCount();
            validGroupCount += executionResult.validGroupCount();
            droppedGroupCount += executionResult.droppedGroupCount();
        }

        return new ToolExecutionAggregate(
                new ArrayList<>(targetCardPassCodes),
                rawGroupCount,
                validGroupCount,
                droppedGroupCount,
                validConditionGroups
        );
    }

    private SearchToolExecutionResult parseSearchToolExecutionResult(String dbResult) throws JsonProcessingException {
        if (!StringUtils.hasText(dbResult)) {
            return new SearchToolExecutionResult(List.of(), 0, 0, 0, List.of());
        }

        JsonNode root = objectMapper.readTree(dbResult);
        if (root == null || root.isNull()) {
            return new SearchToolExecutionResult(List.of(), 0, 0, 0, List.of());
        }

        // 向后兼容：旧版工具输出是纯数组。
        if (root.isArray()) {
            List<String> targets = objectMapper.convertValue(root, new TypeReference<List<String>>() {
            });
            return new SearchToolExecutionResult(targets, 0, 0, 0, List.of());
        }

        List<String> targets = readStringList(root.path("targetCardPassCodes"));
        List<Map<String, Object>> validGroups = readMapList(root.path("validConditionGroups"));

        return new SearchToolExecutionResult(
                targets,
                root.path("rawGroupCount").asInt(0),
                root.path("validGroupCount").asInt(0),
                root.path("droppedGroupCount").asInt(0),
                validGroups
        );
    }

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (item != null && !item.isNull() && StringUtils.hasText(item.asText())) {
                values.add(item.asText().trim());
            }
        }
        return values;
    }

    private List<Map<String, Object>> readMapList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        return objectMapper.convertValue(node, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private Long parsePassCode(String passCode) {
        if (!StringUtils.hasText(passCode)) {
            return null;
        }
        try {
            return Long.valueOf(passCode.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> buildLlmRawResponse(List<ToolCallingIntent> intents,
                                                    SearchSegmentFilterResult segmentFilterResult,
                                                    ToolExecutionAggregate aggregate) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("toolIntents", intents == null ? List.of() : intents);

        Map<String, Object> segmentSummary = new LinkedHashMap<>();
        segmentSummary.put("rawSegmentCount", segmentFilterResult.rawSegmentCount());
        segmentSummary.put("keptSegmentCount", segmentFilterResult.keptSegments().size());
        segmentSummary.put("droppedSegmentCount", segmentFilterResult.droppedSegments().size());
        segmentSummary.put("keptSegments", segmentFilterResult.keptSegments());
        segmentSummary.put("droppedSegments", segmentFilterResult.droppedSegments());
        payload.put("segmentFilterSummary", segmentSummary);

        Map<String, Object> groupSummary = new LinkedHashMap<>();
        groupSummary.put("rawConditionGroups", extractRawConditionGroups(intents));
        groupSummary.put("rawGroupCount", aggregate.rawGroupCount());
        groupSummary.put("validGroupCount", aggregate.validGroupCount());
        groupSummary.put("droppedGroupCount", aggregate.droppedGroupCount());
        groupSummary.put("validConditionGroups", aggregate.validConditionGroups());
        payload.put("groupSummary", groupSummary);

        payload.put("targetCardPassCodes", aggregate.targetCardPassCodes());
        return payload;
    }

    private List<Map<String, Object>> extractRawConditionGroups(List<ToolCallingIntent> intents) {
        if (intents == null || intents.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        for (ToolCallingIntent intent : intents) {
            if (intent == null || !StringUtils.hasText(intent.getToolArgumentsJson())) {
                continue;
            }
            try {
                JsonNode root = objectMapper.readTree(intent.getToolArgumentsJson());
                JsonNode conditionGroups = root.path("conditionGroups");
                if (conditionGroups.isArray()) {
                    groups.addAll(readMapList(conditionGroups));
                    continue;
                }
                groups.add(objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {
                }));
            } catch (Exception e) {
                log.warn("解析工具原始分支失败，toolCallId={}", intent.getToolCallId(), e);
            }
        }
        return groups;
    }

    private record SearchSegmentFilterResult(int rawSegmentCount,
                                             List<String> keptSegments,
                                             List<String> droppedSegments) {
    }

    private record SearchToolExecutionResult(List<String> targetCardPassCodes,
                                             int rawGroupCount,
                                             int validGroupCount,
                                             int droppedGroupCount,
                                             List<Map<String, Object>> validConditionGroups) {
    }

    private record ToolExecutionAggregate(List<String> targetCardPassCodes,
                                          int rawGroupCount,
                                          int validGroupCount,
                                          int droppedGroupCount,
                                          List<Map<String, Object>> validConditionGroups) {
    }
}
