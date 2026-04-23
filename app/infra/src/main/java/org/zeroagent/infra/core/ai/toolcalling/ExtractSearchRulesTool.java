package org.zeroagent.infra.core.ai.toolcalling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingExecutor;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationQO;
import org.zeroagent.domain.core.card.model.CardSubTypeEnum;
import org.zeroagent.domain.core.card.model.CardTypeEnum;
import org.zeroagent.domain.core.card.service.CardInformationRepository;

import java.util.List;

/**
 * 用于从卡牌信息中抽取 结构化信息 建立图谱关系
 * @author Nuk3m1
 * @version 2026年04月20日  23时13分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractSearchRulesTool implements ToolCallingExecutor {
    private final ObjectMapper              objectMapper;
    private final CardInformationRepository cardInformationRepository;

    @Override
    public ToolCallingEnum getToolType() {
        return ToolCallingEnum.EXTRACT_SEARCH_INFORMATION_FROM_CARD;
    }

    @Override
    public ObjectNode getToolDefinitionNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "function");
        ObjectNode function = root.putObject("function");
        function.put("name", ToolCallingEnum.EXTRACT_SEARCH_INFORMATION_FROM_CARD.getFunctionName());
        function.put("description", "当用户或系统需要提取卡牌的【检索范围/作用目标】时调用此工具。必须严格根据卡牌的自然语言描述，提取出检索规则。不属于检索条件的字段必须留空（不输出该字段）。");
        function.put("strict", true); // 开启 strict 强制模式

        ObjectNode parameters = function.putObject("parameters");
        parameters.put("type", "object");
        parameters.put("additionalProperties", false); // 开启 strict 强制模式
        ObjectNode properties = parameters.putObject("properties");

        // 字段(archetypes)
        ObjectNode archetypes = properties.putObject("archetypes");
        archetypes.put("type", "array");
        archetypes.putObject("items").put("type", "string");
        archetypes.put("description", "明确提到的卡牌字段名，如 ['星辰', '英雄', '烙印']。只提取名称，不要带上'字段'两字。如果检索范围没有提到任何字段，必须返回空数组[]，绝对不能返回其他值.");

        // 严格枚举部分 (主类型，子类型，属性，种族，攻击力，防御力，星级)
        // 主类型
        ObjectNode mainType = properties.putObject("mainType");
        mainType.putArray("type").add("string").add("null");
        mainType.putArray("enum")
                .add(CardTypeEnum.MONSTER.name())
                .add(CardTypeEnum.MAGIC.name())
                .add(CardTypeEnum.TRAP.name())
                .addNull();
        mainType.put("description", "主卡片类型。如果检索范围没有限制主类型，必须输出null");
        // 子类型
        ObjectNode subTypes = properties.putObject("subTypes");
        subTypes.put("type", "array");
        subTypes.put("description", "子类型，如怪兽的'调整'‘融合’‘同调’，魔法的‘永续’‘速攻’‘通常’，陷阱的‘反击’’通常‘等. 如果检索范围和目标子类型无关，必须返回空数组[],绝对不能返回其他任何值");
        ObjectNode subTypeItems = subTypes.putObject("items");
        subTypeItems.put("type", "string");
        subTypeItems.putArray("enum")
                .add(CardSubTypeEnum.MAGIC_NORMAL.name())           .add(CardSubTypeEnum.MAGIC_CONTINUOUS.name())   .add(CardSubTypeEnum.MAGIC_EQUIP.name())
                .add(CardSubTypeEnum.MAGIC_RITUAL.name())           .add(CardSubTypeEnum.MAGIC_FIELD.name())        .add(CardSubTypeEnum.MAGIC_QUICKPLAY.name())
                .add(CardSubTypeEnum.TRAP_NORMAL.name())            .add(CardSubTypeEnum.TRAP_CONTINUOUS.name())    .add(CardSubTypeEnum.TRAP_COUNTER.name())
                .add(CardSubTypeEnum.MONSTER_NORMAL.name())         .add(CardSubTypeEnum.MONSTER_EFFECT.name())     .add(CardSubTypeEnum.MONSTER_TUNER.name())
                .add(CardSubTypeEnum.MONSTER_FUSION.name())         .add(CardSubTypeEnum.MONSTER_SYNCHRO.name())    .add(CardSubTypeEnum.MONSTER_XYZ.name())
                .add(CardSubTypeEnum.MONSTER_PENDULUM.name())       .add(CardSubTypeEnum.MONSTER_LINK.name())       .add(CardSubTypeEnum.MONSTER_TRAP.name())
                .add(CardSubTypeEnum.MONSTER_SPIRIT.name())         .add(CardSubTypeEnum.MONSTER_UNION.name())      .add(CardSubTypeEnum.MONSTER_GEMINI.name())
                .add(CardSubTypeEnum.MONSTER_TOKEN.name())          .add(CardSubTypeEnum.MONSTER_FLIP.name())       .add(CardSubTypeEnum.MONSTER_TOON.name())
                .add(CardSubTypeEnum.MONSTER_SPECIAL_SUMMON.name()) .add(CardSubTypeEnum.MONSTER_RITUAL.name());
        // 属性
        ObjectNode attribute = properties.putObject("attribute");
        attribute.putArray("type").add("string").add("null");
        attribute.putArray("enum")
                .add("地").add("火").add("水").add("风").add("光").add("暗").add("神")
                .addNull();
        attribute.put("description", "检索条件的怪兽属性，如果没有属性的检索条件，则必须输出null");
        //种族
        ObjectNode race = properties.putObject("race");
        race.putArray("type").add("string").add("null");
        race.putArray("enum")
                .add("战士族").add("魔法师族").add("天使族").add("恶魔族").add("不死族").add("机械族")
                .add("水族").add("炎族").add("岩石族").add("鸟兽族").add("植物族").add("昆虫族")
                .add("雷族").add("龙族").add("兽族").add("兽战士族").add("恐龙族").add("鱼族")
                .add("海龙族").add("爬虫类族").add("念动力族").add("幻神兽族").add("创造神族").add("幻龙族")
                .add("电子界族").add("幻想魔族")
                .addNull();
        race.put("description", "检索条件的怪兽种族，如果没有种族的检索条件，则必须输出null");
        // 数值及操作符
        // 星级操作符与星级
        ObjectNode levelOperator = properties.putObject("levelOperator");
        levelOperator.putArray("type").add("string").add("null");
        levelOperator.putArray("enum").add("=").add(">=").add(">").add("<").add("<=")
                .addNull();
        levelOperator.put("description", "星级的数学比较符，如'四星以下'选'<='，如果不存在星级检索条件，输出null");

        ObjectNode level = properties.putObject("level");
        level.putArray("type").add("integer").add("null");
        level.put("description", "具体的星级条件数值。如果检索条件没有星级限制，必须输出 null.");
        // 攻击力操作符与攻击力
        ObjectNode atkOperator = properties.putObject("atkOperator");
        atkOperator.putArray("type").add("string").add("null");
        atkOperator.putArray("enum").add("=").add(">=").add(">").add("<").add("<=")
                .addNull();
        atkOperator.put("description", "攻击力的数学比较符，如'2000攻击力以下'选'<='，如果不存在攻击力检索条件，必须输出null");

        ObjectNode atk = properties.putObject("atk");
        atk.putArray("type").add("integer").add("null");
        atk.put("description", "具体的攻击力数值，如果检索条件没有攻击力限制，必须输出 null");
        // 防御力操作符与防御力
        ObjectNode defOperator = properties.putObject("defOperator");
        defOperator.putArray("type").add("string").add("null");
        defOperator.putArray("enum").add("=").add(">=").add(">").add("<").add("<=")
                .addNull();
        defOperator.put("description", "防御力的数学比较符，如'2000防御力以下'选'<='，如果不存在防御力检索条件，必须输出null");

        ObjectNode def = properties.putObject("def");
        def.putArray("type").add("integer").add("null");
        def.put("description", "具体的防御力数值，如果检索条件没有防御力限制，必须输出null");
        // Strict 强制声明模式 [required] 数组设置
        ArrayNode required = parameters.putArray("required");
        required.add("archetypes")
                .add("mainType").add("subTypes")
                .add("attribute").add("race")
                .add("levelOperator").add("level")
                .add("atkOperator").add("atk")
                .add("defOperator").add("def");
        return root;
    }

    @Override
    public String execute(String arguments) {
        log.info("[抽取卡牌关系] LLM原始参数输出：{}", arguments);
        try {
            CardInformationQO qo = objectMapper.readValue(arguments, CardInformationQO.class);
            List<Long> cardIds = cardInformationRepository.fetchBatchByCondition(qo)
                    .stream().map(CardInformation::getPasscode)
                    .map(Long::parseLong)
                    .toList();
            log.info("目标卡牌ID集合：{}", cardIds);
            return objectMapper.writeValueAsString(cardIds);
        } catch (JsonProcessingException e) {
            log.error("工具调用参数解析失败", e);
            throw new RuntimeException("工具调用参数解析失败", e);
        }
    }
}
