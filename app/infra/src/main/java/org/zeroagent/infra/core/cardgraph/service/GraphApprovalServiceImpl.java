package org.zeroagent.infra.core.cardgraph.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.core.ai.chat.model.SystemPromptPool;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;
import org.zeroagent.domain.core.ai.chat.model.message.Message;
import org.zeroagent.domain.core.ai.chat.model.message.SystemMessage;
import org.zeroagent.domain.core.ai.chat.model.message.UserMessage;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingFactory;
import org.zeroagent.domain.core.cardgraph.model.GraphRelationTypeEnum;
import org.zeroagent.domain.core.grapherror.model.GraphApprovalDecision;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.service.GraphApprovalService;
import org.zeroagent.infra.core.ai.model.DouBaoChatRequest;
import org.zeroagent.infra.core.ai.toolcalling.ToolCallingExecutionResult;
import org.zeroagent.infra.core.ai.toolcalling.ToolCallingExecutionTemplate;
import org.zeroagent.infra.integration.llm.doubao.DouBaoChatProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图谱审批服务实现
 * @author Nuk3m1
 * @version 2026年04月23日  15时30分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GraphApprovalServiceImpl implements GraphApprovalService {
    private static final String SUPPORTED_RELATION_PROMPT = Arrays.stream(GraphRelationTypeEnum.values())
            .map(item -> item.name() + ":" + item.getDescription())
            .collect(Collectors.joining("；"));

    private final ObjectMapper                  objectMapper;
    private final DouBaoChatProperties          douBaoChatProperties;
    private final ToolCallingFactory            toolCallingFactory;
    private final ToolCallingExecutionTemplate  toolCallingExecutionTemplate;

    @Override
    public GraphApprovalDecision approve(GraphErrorLog graphErrorLog) {
        if (graphErrorLog == null) {
            return failedDecision("审批工单为空");
        }
        if (!StringUtils.hasText(graphErrorLog.getGraphRelationType())) {
            return failedDecision("缺少关系类型");
        }
        GraphRelationTypeEnum relationTypeEnum = parseRelationType(graphErrorLog.getGraphRelationType());
        if (relationTypeEnum == null) {
            return failedDecision("当前版本不支持该关系类型，支持范围: " + SUPPORTED_RELATION_PROMPT);
        }
        if (graphErrorLog.getSourceCardId() == null || graphErrorLog.getTargetCardId() == null) {
            return failedDecision("源/目标卡密缺失");
        }
        if (graphErrorLog.getSourceCardId().equals(graphErrorLog.getTargetCardId())) {
            return failedDecision("源卡与目标卡相同，疑似异常关系");
        }
        if (!StringUtils.hasText(graphErrorLog.getSourceCardEffect())) {
            return failedDecision("缺少源卡牌效果，无法审批");
        }
        try {
            // 审批Agent对话
            DouBaoChatRequest requestBody = buildRequest(graphErrorLog);
            ToolCallingExecutionResult executionResult = toolCallingExecutionTemplate
                    .execute(requestBody, "审批阶段未发生工具调用")
                    .block();
            if (executionResult == null || executionResult.getResults() == null || executionResult.getResults().isEmpty()) {
                return failedDecision("审批工具执行结果为空");
            }
            return mapDecision(graphErrorLog, executionResult.getResults());
        } catch (Exception e) {
            log.error("审批 Agent 执行失败，logId={}", graphErrorLog.getId(), e);
            return failedDecision("审批Agent执行失败: " + e.getMessage());
        }
    }

    private DouBaoChatRequest buildRequest(GraphErrorLog graphErrorLog) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(MediaType.TEXT, SystemPromptPool.GRAPH_RELATION_APPROVAL_PROMPT.formatted(SUPPORTED_RELATION_PROMPT)));
        messages.add(new UserMessage(MediaType.TEXT, buildUserPrompt(graphErrorLog)));

        DouBaoChatRequest requestBody = new DouBaoChatRequest()
                .setModel(douBaoChatProperties.getModel())
                .setMessages(messages);
        if (requestBody.getTools() == null || requestBody.getTools().isEmpty()) {
            List<ObjectNode> allTools = toolCallingFactory.getToolDefinitionNodes(ToolCallingEnum.EXTRACT_APPROVAL_DECISION);
            requestBody.setTools(allTools);
        }
        return requestBody;
    }

    private String buildUserPrompt(GraphErrorLog graphErrorLog) {
        return """
                待审批关系候选如下：
                sourceCardPassCode: %s
                sourceCardName: %s
                sourceCardEffect: %s
                targetCardName: %s
                targetCardEffect: %s
                relationType: %s
                请仅通过工具调用输出结构化审批结果。
                """.formatted(
                graphErrorLog.getSourceCardId(),
                defaultIfBlank(graphErrorLog.getSourceCardName()),
                defaultIfBlank(graphErrorLog.getSourceCardEffect()),
                defaultIfBlank(graphErrorLog.getTargetCardName()),
                defaultIfBlank(graphErrorLog.getTargetCardEffect()),
                graphErrorLog.getGraphRelationType()

        );
    }

    private GraphApprovalDecision mapDecision(GraphErrorLog graphErrorLog, List<ToolCallingBizResult> results) throws Exception {
        ToolCallingBizResult toolResult = results.stream()
                .filter(item -> ToolCallingEnum.EXTRACT_APPROVAL_DECISION.getFunctionName().equals(item.getToolName()))
                .findFirst()
                .orElse(results.getFirst());

        ApprovalToolResult parsedResult = objectMapper.readValue(toolResult.getDbResult(), ApprovalToolResult.class);
        if (parsedResult == null) {
            return failedDecision("审批结果为空");
        }
        if (!StringUtils.hasText(parsedResult.relationType)) {
            return failedDecision("审批结果缺少 relationType");
        }
        if (!parsedResult.relationType.equals(graphErrorLog.getGraphRelationType())) {
            return failedDecision("审批返回关系类型不匹配: " + parsedResult.relationType);
        }
        String reason = StringUtils.hasText(parsedResult.reason)
                ? parsedResult.reason.trim()
                : parsedResult.approved ? "AUTO_APPROVED_BY_LLM" : "AUTO_REJECTED_BY_LLM";
        return new GraphApprovalDecision()
                .setApproved(parsedResult.approved)
                .setReason(reason);
    }

    private GraphRelationTypeEnum parseRelationType(String relationType) {
        if (!StringUtils.hasText(relationType)) {
            return null;
        }
        try {
            return GraphRelationTypeEnum.valueOf(relationType.trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    private String defaultIfBlank(String text) {
        return StringUtils.hasText(text) ? text : "";
    }

    private GraphApprovalDecision failedDecision(String reason) {
        return new GraphApprovalDecision().setApproved(false).setReason(reason);
    }

    private static class ApprovalToolResult {
        public boolean approved;
        public String reason;
        public String relationType;
    }
}
