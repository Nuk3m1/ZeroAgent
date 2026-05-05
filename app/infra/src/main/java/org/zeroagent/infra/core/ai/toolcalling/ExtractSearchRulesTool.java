package org.zeroagent.infra.core.ai.toolcalling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingExecutor;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationQO;
import org.zeroagent.domain.core.card.model.CardSubTypeEnum;
import org.zeroagent.domain.core.card.model.CardTypeEnum;
import org.zeroagent.domain.core.card.service.CardInformationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SEARCH 专用语义抽取工具。
 * @author Nuk3m1
 * @version 2026年04月20日  23时13分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExtractSearchRulesTool implements ToolCallingExecutor {
    private static final Set<String> LEGAL_NUMERIC_OPERATORS = Set.of("=", ">=", ">", "<", "<=");
    private static final Set<String> LEGAL_MAIN_TYPES = Set.of(
            CardTypeEnum.MONSTER.name(),
            CardTypeEnum.MAGIC.name(),
            CardTypeEnum.TRAP.name()
    );
    private static final Set<String> LEGAL_SUB_TYPES = Arrays.stream(CardSubTypeEnum.values())
            .map(CardSubTypeEnum::name)
            .collect(Collectors.toUnmodifiableSet());

    private final ObjectMapper objectMapper;
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
        function.put("description", "仅用于提取 SEARCH 关系（从卡组加入手卡）。输出分支结构：conditionGroups（分支 OR，分支内 AND）。禁止输出与特殊召唤、墓地回收、除外回收等非 SEARCH 动作相关的参数。");
        function.put("strict", true);

        ObjectNode parameters = function.putObject("parameters");
        parameters.put("type", "object");
        parameters.put("additionalProperties", false);

        ObjectNode properties = parameters.putObject("properties");

        ObjectNode groupOperator = properties.putObject("groupOperator");
        groupOperator.put("type", "string");
        groupOperator.putArray("enum").add("OR");
        groupOperator.put("description", "分支组逻辑，当前固定 OR");

        ObjectNode conditionGroups = properties.putObject("conditionGroups");
        conditionGroups.put("type", "array");
        conditionGroups.put("description", "检索条件分支列表，分支之间是 OR 关系；每个分支内部字段是 AND 关系");

        ObjectNode groupItems = conditionGroups.putObject("items");
        groupItems.put("type", "object");
        groupItems.put("additionalProperties", false);
        ObjectNode groupProperties = groupItems.putObject("properties");
        appendConditionProperties(groupProperties);
        ArrayNode groupRequired = groupItems.putArray("required");
        appendConditionRequiredFields(groupRequired);

        ArrayNode required = parameters.putArray("required");
        required.add("groupOperator");
        required.add("conditionGroups");
        return root;
    }

    @Override
    public String execute(String arguments) {
        log.info("[抽取卡牌关系] LLM原始参数输出：{}", arguments);
        try {
            List<CardInformationQO> rawGroups = parseRawGroups(arguments);
            GroupValidationResult validationResult = validateGroups(rawGroups);

            LinkedHashSet<Long> targetPassCodeSet = new LinkedHashSet<>();
            if (validationResult.validGroupCount() > 0) {
                for (CardInformationQO qo : validationResult.validGroups()) {
                    List<CardInformation> cards = cardInformationRepository.fetchBatchByCondition(qo);
                    for (CardInformation card : cards) {
                        Long passCode = parsePassCode(card.getPasscode());
                        if (passCode != null) {
                            targetPassCodeSet.add(passCode);
                        }
                    }
                }
            }

            List<String> targetCardPassCodes = targetPassCodeSet.stream()
                    .map(String::valueOf)
                    .toList();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("targetCardPassCodes", targetCardPassCodes);
            result.put("rawGroupCount", validationResult.rawGroupCount());
            result.put("validGroupCount", validationResult.validGroupCount());
            result.put("droppedGroupCount", validationResult.droppedGroupCount());
            result.put("validConditionGroups", validationResult.validGroups());

            log.info("[抽取卡牌关系] group统计 raw={}, valid={}, dropped={}, 命中目标卡牌数={}",
                    validationResult.rawGroupCount(),
                    validationResult.validGroupCount(),
                    validationResult.droppedGroupCount(),
                    targetCardPassCodes.size());
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("工具调用参数解析失败", e);
            throw new RuntimeException("工具调用参数解析失败", e);
        }
    }

    private List<CardInformationQO> parseRawGroups(String arguments) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(arguments);
        if (root == null || root.isNull()) {
            return List.of();
        }

        JsonNode conditionGroupsNode = root.path("conditionGroups");
        if (conditionGroupsNode.isArray()) {
            List<CardInformationQO> conditionGroups = new ArrayList<>();
            for (JsonNode groupNode : conditionGroupsNode) {
                conditionGroups.add(objectMapper.treeToValue(groupNode, CardInformationQO.class));
            }
            return conditionGroups;
        }

        // 兼容旧版单层 schema：直接将根节点映射为单个分支。
        return List.of(objectMapper.treeToValue(root, CardInformationQO.class));
    }

    private GroupValidationResult validateGroups(List<CardInformationQO> rawGroups) {
        if (rawGroups == null || rawGroups.isEmpty()) {
            return new GroupValidationResult(0, 0, 0, List.of());
        }

        List<CardInformationQO> validGroups = new ArrayList<>();
        int droppedGroupCount = 0;
        for (CardInformationQO group : rawGroups) {
            CardInformationQO normalizedGroup = normalizeGroup(group);
            if (normalizedGroup == null || !isValidGroup(normalizedGroup)) {
                droppedGroupCount++;
                continue;
            }
            validGroups.add(normalizedGroup);
        }

        return new GroupValidationResult(
                rawGroups.size(),
                validGroups.size(),
                droppedGroupCount,
                validGroups
        );
    }

    private CardInformationQO normalizeGroup(CardInformationQO group) {
        if (group == null) {
            return null;
        }
        return new CardInformationQO()
                .setArchetypes(normalizeList(group.getArchetypes(), false))
                .setMainType(normalizeUpper(group.getMainType()))
                .setSubTypes(normalizeList(group.getSubTypes(), true))
                .setAttribute(normalizeText(group.getAttribute()))
                .setRace(normalizeText(group.getRace()))
                .setLevelOperator(normalizeText(group.getLevelOperator()))
                .setLevel(group.getLevel())
                .setAtkOperator(normalizeText(group.getAtkOperator()))
                .setAtk(group.getAtk())
                .setDefOperator(normalizeText(group.getDefOperator()))
                .setDef(group.getDef());
    }

    private boolean isValidGroup(CardInformationQO group) {
        if (!isMainTypeValid(group.getMainType())) {
            return false;
        }
        if (!isSubTypesValid(group.getSubTypes())) {
            return false;
        }
        if (!isNumericPairValid(group.getLevelOperator(), group.getLevel())) {
            return false;
        }
        if (!isNumericPairValid(group.getAtkOperator(), group.getAtk())) {
            return false;
        }
        if (!isNumericPairValid(group.getDefOperator(), group.getDef())) {
            return false;
        }
        return hasConstraint(group);
    }

    private boolean isMainTypeValid(String mainType) {
        if (!StringUtils.hasText(mainType)) {
            return true;
        }
        return LEGAL_MAIN_TYPES.contains(mainType);
    }

    private boolean isSubTypesValid(List<String> subTypes) {
        if (subTypes == null || subTypes.isEmpty()) {
            return true;
        }
        return subTypes.stream().allMatch(LEGAL_SUB_TYPES::contains);
    }

    private boolean isNumericPairValid(String operator, Number value) {
        boolean hasOperator = StringUtils.hasText(operator);
        if (!hasOperator && value == null) {
            return true;
        }
        if (!hasOperator || value == null) {
            return false;
        }
        return LEGAL_NUMERIC_OPERATORS.contains(operator);
    }

    private boolean hasConstraint(CardInformationQO group) {
        return hasTextElement(group.getArchetypes())
                || hasTextElement(group.getSubTypes())
                || StringUtils.hasText(group.getMainType())
                || StringUtils.hasText(group.getAttribute())
                || StringUtils.hasText(group.getRace())
                || hasValidNumericConstraint(group.getLevelOperator(), group.getLevel())
                || hasValidNumericConstraint(group.getAtkOperator(), group.getAtk())
                || hasValidNumericConstraint(group.getDefOperator(), group.getDef());
    }

    private boolean hasValidNumericConstraint(String operator, Number value) {
        return StringUtils.hasText(operator)
                && value != null
                && LEGAL_NUMERIC_OPERATORS.contains(operator);
    }

    private boolean hasTextElement(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        return values.stream().anyMatch(StringUtils::hasText);
    }

    private List<String> normalizeList(List<String> values, boolean upperCase) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            String text = value.trim();
            if (upperCase) {
                text = text.toUpperCase(Locale.ROOT);
            }
            normalized.add(text);
        }
        return new ArrayList<>(normalized);
    }

    private String normalizeUpper(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Long parsePassCode(String passCode) {
        if (!StringUtils.hasText(passCode)) {
            return null;
        }
        try {
            return Long.valueOf(passCode.trim());
        } catch (NumberFormatException ex) {
            log.warn("目标卡密不是合法数字，跳过去重: {}", passCode);
            return null;
        }
    }

    private void appendConditionProperties(ObjectNode properties) {
        ObjectNode archetypes = properties.putObject("archetypes");
        archetypes.put("type", "array");
        archetypes.putObject("items").put("type", "string");
        archetypes.put("description", "明确提到的字段名数组，如 ['星辰','英雄','烙印']，无约束时返回空数组");

        ObjectNode mainType = properties.putObject("mainType");
        mainType.putArray("type").add("string").add("null");
        mainType.putArray("enum")
                .add(CardTypeEnum.MONSTER.name())
                .add(CardTypeEnum.MAGIC.name())
                .add(CardTypeEnum.TRAP.name())
                .addNull();
        mainType.put("description", "主类型，无约束时返回 null");

        ObjectNode subTypes = properties.putObject("subTypes");
        subTypes.put("type", "array");
        subTypes.put("description", "子类型数组，无约束时返回空数组");
        ObjectNode subTypeItems = subTypes.putObject("items");
        subTypeItems.put("type", "string");
        subTypeItems.putArray("enum")
                .add(CardSubTypeEnum.MAGIC_NORMAL.name()).add(CardSubTypeEnum.MAGIC_CONTINUOUS.name()).add(CardSubTypeEnum.MAGIC_EQUIP.name())
                .add(CardSubTypeEnum.MAGIC_RITUAL.name()).add(CardSubTypeEnum.MAGIC_FIELD.name()).add(CardSubTypeEnum.MAGIC_QUICKPLAY.name())
                .add(CardSubTypeEnum.TRAP_NORMAL.name()).add(CardSubTypeEnum.TRAP_CONTINUOUS.name()).add(CardSubTypeEnum.TRAP_COUNTER.name())
                .add(CardSubTypeEnum.MONSTER_NORMAL.name()).add(CardSubTypeEnum.MONSTER_EFFECT.name()).add(CardSubTypeEnum.MONSTER_TUNER.name())
                .add(CardSubTypeEnum.MONSTER_FUSION.name()).add(CardSubTypeEnum.MONSTER_SYNCHRO.name()).add(CardSubTypeEnum.MONSTER_XYZ.name())
                .add(CardSubTypeEnum.MONSTER_PENDULUM.name()).add(CardSubTypeEnum.MONSTER_LINK.name()).add(CardSubTypeEnum.MONSTER_TRAP.name())
                .add(CardSubTypeEnum.MONSTER_SPIRIT.name()).add(CardSubTypeEnum.MONSTER_UNION.name()).add(CardSubTypeEnum.MONSTER_GEMINI.name())
                .add(CardSubTypeEnum.MONSTER_TOKEN.name()).add(CardSubTypeEnum.MONSTER_FLIP.name()).add(CardSubTypeEnum.MONSTER_TOON.name())
                .add(CardSubTypeEnum.MONSTER_SPECIAL_SUMMON.name()).add(CardSubTypeEnum.MONSTER_RITUAL.name());

        ObjectNode attribute = properties.putObject("attribute");
        attribute.putArray("type").add("string").add("null");
        attribute.putArray("enum")
                .add("地").add("火").add("水").add("风").add("光").add("暗").add("神")
                .addNull();
        attribute.put("description", "怪兽属性，无约束时返回 null");

        ObjectNode race = properties.putObject("race");
        race.putArray("type").add("string").add("null");
        race.putArray("enum")
                .add("战士族").add("魔法师族").add("天使族").add("恶魔族").add("不死族").add("机械族")
                .add("水族").add("炎族").add("岩石族").add("鸟兽族").add("植物族").add("昆虫族")
                .add("雷族").add("龙族").add("兽族").add("兽战士族").add("恐龙族").add("鱼族")
                .add("海龙族").add("爬虫类族").add("念动力族").add("幻神兽族").add("创造神族").add("幻龙族")
                .add("电子界族").add("幻想魔族")
                .addNull();
        race.put("description", "怪兽种族，无约束时返回 null");

        ObjectNode levelOperator = properties.putObject("levelOperator");
        levelOperator.putArray("type").add("string").add("null");
        levelOperator.putArray("enum").add("=").add(">=").add(">").add("<").add("<=").addNull();
        levelOperator.put("description", "星级比较符，无约束时返回 null");

        ObjectNode level = properties.putObject("level");
        level.putArray("type").add("integer").add("null");
        level.put("description", "星级数值，无约束时返回 null");

        ObjectNode atkOperator = properties.putObject("atkOperator");
        atkOperator.putArray("type").add("string").add("null");
        atkOperator.putArray("enum").add("=").add(">=").add(">").add("<").add("<=").addNull();
        atkOperator.put("description", "攻击力比较符，无约束时返回 null");

        ObjectNode atk = properties.putObject("atk");
        atk.putArray("type").add("integer").add("null");
        atk.put("description", "攻击力数值，无约束时返回 null");

        ObjectNode defOperator = properties.putObject("defOperator");
        defOperator.putArray("type").add("string").add("null");
        defOperator.putArray("enum").add("=").add(">=").add(">").add("<").add("<=").addNull();
        defOperator.put("description", "防御力比较符，无约束时返回 null");

        ObjectNode def = properties.putObject("def");
        def.putArray("type").add("integer").add("null");
        def.put("description", "防御力数值，无约束时返回 null");
    }

    private void appendConditionRequiredFields(ArrayNode required) {
        required.add("archetypes")
                .add("mainType")
                .add("subTypes")
                .add("attribute")
                .add("race")
                .add("levelOperator")
                .add("level")
                .add("atkOperator")
                .add("atk")
                .add("defOperator")
                .add("def");
    }

    private record GroupValidationResult(int rawGroupCount,
                                         int validGroupCount,
                                         int droppedGroupCount,
                                         List<CardInformationQO> validGroups) {
    }
}
