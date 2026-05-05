package org.zeroagent.infra.core.ai.toolcalling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingExecutor;
import org.zeroagent.domain.core.cardgraph.model.GraphRelationTypeEnum;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 审批意图抽取工具，负责将 LLM 输出约束为结构化审批结果。
 * @author Nuk3m1
 * @version 2026年04月23日  17时03分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractApprovalDecisionTool implements ToolCallingExecutor {
    private static final String SUPPORTED_RELATION_PROMPT = Arrays.stream(GraphRelationTypeEnum.values())
            .map(item -> item.name() + ":" + item.getDescription())
            .collect(Collectors.joining("；"));

    private final ObjectMapper objectMapper;

    @Override
    public ToolCallingEnum getToolType() {
        return ToolCallingEnum.EXTRACT_APPROVAL_DECISION;
    }

    @Override
    public ObjectNode getToolDefinitionNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "function");
        ObjectNode function = root.putObject("function");
        function.put("name", ToolCallingEnum.EXTRACT_APPROVAL_DECISION.getFunctionName());
        function.put("description", "用于图谱关系审批结论抽取，输出是否通过、原因及关系类型。支持关系类型: " + SUPPORTED_RELATION_PROMPT);
        function.put("strict", true);

        ObjectNode parameters = function.putObject("parameters");
        parameters.put("type", "object");
        parameters.put("additionalProperties", false);
        ObjectNode properties = parameters.putObject("properties");

        ObjectNode approved = properties.putObject("approved");
        approved.put("type", "boolean");
        approved.put("description", "审批结论，true 表示通过，false 表示拒绝");

        ObjectNode reason = properties.putObject("reason");
        reason.put("type", "string");
        reason.put("description", "审批原因，必须给出简短明确的理由");

        ObjectNode relationType = properties.putObject("relationType");
        relationType.put("type", "string");
        ArrayNode relationTypeEnum = relationType.putArray("enum");
        for (GraphRelationTypeEnum item : GraphRelationTypeEnum.values()) {
            relationTypeEnum.add(item.name());
        }
        relationType.put("description", "关系类型，必须属于支持范围: " + SUPPORTED_RELATION_PROMPT);

        parameters.putArray("required")
                .add("approved")
                .add("reason")
                .add("relationType");
        return root;
    }

    @Override
    public String execute(String arguments) {
        log.info("[图谱审批] LLM原始参数输出：{}", arguments);
        try {
            ApprovalDecisionArgs args = objectMapper.readValue(arguments, ApprovalDecisionArgs.class);
            GraphRelationTypeEnum relationTypeEnum = parseRelationType(args.relationType);
            boolean approved = Boolean.TRUE.equals(args.approved);
            String reason = StringUtils.hasText(args.reason)
                    ? args.reason.trim()
                    : approved ? "AUTO_APPROVED_BY_LLM" : "AUTO_REJECTED_BY_LLM";

            ObjectNode normalized = objectMapper.createObjectNode();
            normalized.put("approved", approved);
            normalized.put("reason", reason);
            normalized.put("relationType", relationTypeEnum.name());
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException e) {
            log.error("审批工具调用参数解析失败", e);
            throw new RuntimeException("审批工具调用参数解析失败", e);
        }
    }

    private GraphRelationTypeEnum parseRelationType(String relationType) {
        if (!StringUtils.hasText(relationType)) {
            throw new IllegalArgumentException("relationType 不能为空");
        }
        try {
            return GraphRelationTypeEnum.valueOf(relationType.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("relationType 非法，支持范围: " + SUPPORTED_RELATION_PROMPT, e);
        }
    }

    private static class ApprovalDecisionArgs {
        public Boolean approved;
        public String relationType;
        public String reason;
    }
}
