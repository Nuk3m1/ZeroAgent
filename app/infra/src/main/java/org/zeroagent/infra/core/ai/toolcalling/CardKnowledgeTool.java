package org.zeroagent.infra.core.ai.toolcalling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Debug;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingExecutor;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.cardgraph.service.CardGraphRepository;
import org.zeroagent.infra.integration.neo4j.serevice.Neo4jCardGraphRepositoryImpl;
import org.zeroagent.infra.utils.DebugUtil;

import java.util.List;

/**
 * 卡牌知识工具 - 通过 卡名 获得 卡牌信息 及其 图谱关系
 * @author Nuk3m1
 * @version 2026年04月15日  23时14分
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CardKnowledgeTool implements ToolCallingExecutor {
    private final CardInformationRepository     cardInformationRepository;
    private final CardGraphRepository           cardGraphRepository;
    private final ObjectMapper                  objectMapper;
    @Override
    public ToolCallingEnum getToolType() {
        return ToolCallingEnum.GET_CARD_KNOWLEDGE_BY_NAME;
    }

    @Override
    public ObjectNode getToolDefinitionNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "function");

        ObjectNode function = root.putObject("function");
        // 这里的名字必须和枚举里的 getFunctionName() 保持绝对一致
        function.put("name", ToolCallingEnum.GET_CARD_KNOWLEDGE_BY_NAME.getFunctionName());
        function.put("description", "根据用户提供的卡牌名称(剥离描述性文本)，获取卡片详细信息，并从知识图谱中获取该卡牌的所有关联关系（如属于什么字段，能检索哪些卡等）。");
        function.put("strict", true); //  开启 strict 模式

        ObjectNode parameters = function.putObject("parameters");
        parameters.put("type", "object");
        parameters.put("additionalProperties", false); //  strict 模式强制要求

        ObjectNode properties = parameters.putObject("properties");

        // 定义唯一的入参：card_name
        ObjectNode cardName = properties.putObject("card_name");
        cardName.put("type", "string");
        cardName.put("description", "【极其重要】必须是游戏王官方确切的、绝对正确的卡片全名（如'深渊的神兽 狄斯·帕特尔'）。\" +\n" +
                "    \"严禁直接传入用户的特征描述（如'十星同调的深渊神兽'）。\" +\n" +
                "    \"如果用户提供的是特征、外号或描述，你必须先在你的思考链(Reasoning)中推断、回忆出它的真实官方卡名，然后再将那个唯一的真实卡名作为此参数传入！");

        //  严格模式强制要求所有字段必须放入 required 列表
        ArrayNode required = parameters.putArray("required");
        required.add("card_name");

        return root;
    }

    @Override
    public String execute(String arguments) {
        try {

            // 解析大模型参数
            JsonNode args = objectMapper.readTree(arguments);
            DebugUtil.printJsonObject(objectMapper, arguments);
            String fuzzyName = args.get("card_name").asText();
            // 数据库模糊匹配
            CardInformation card = cardInformationRepository.getCardByFuzzyName(fuzzyName);
            if (card == null || card.getPasscode() == null) {
                return "{\"error\": \"未在数据库中找到与[" + fuzzyName + "]匹配的卡牌，尝试剥离描述性文本重试或提醒用户检查卡名。\"}";
            }
            String passcode = card.getPasscode();
            // 图数据库获取 卡牌关系
            List<String> relationships = cardGraphRepository.getRelationships(passcode);

            ObjectNode resultNode = objectMapper.createObjectNode();
            resultNode.put("matched_passcode", passcode);
            resultNode.put("relation_count", relationships.size());
            resultNode.put("card_real_name", card.getName());
            resultNode.put("card_information", objectMapper.valueToTree(card.getEffect()));
            ArrayNode relArray = resultNode.putArray("relationships");
            if (relationships != null && !relationships.isEmpty()) {
                relationships.forEach(relArray::add);
            }
            DebugUtil.printJsonObject(objectMapper, resultNode);
            return resultNode.toString();


        } catch (Exception e) {
            log.error("工具调用: {} 异常 ", this.getToolType().getFunctionName(), e);
            return "{\"error\": \"工具执行内部异常\"}";
        }
    }
}
