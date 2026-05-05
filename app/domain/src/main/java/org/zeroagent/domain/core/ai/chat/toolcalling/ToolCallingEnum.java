package org.zeroagent.domain.core.ai.chat.toolcalling;


import lombok.Getter;

/**
 * 函数调用注册表
 * 所有工具调用的函数必须在这里注册，通过枚举类传递
 */
@Getter
public enum ToolCallingEnum {
    GET_CARD_KNOWLEDGE_BY_NAME("getCardKnowledgeByName", " 通过 卡名 获得 卡牌信息 及其 图谱关系 "),
    EXTRACT_SEARCH_INFORMATION_FROM_CARD("extractSearchInformationFromCard", "用于抽取从卡组加入手卡（SEARCH）的结构化检索条件"),
    EXTRACT_APPROVAL_DECISION("extractApprovalDecision", "用于对图谱关系候选进行结构化审批")

    ;
    private final String functionName;
    private final String description;

    ToolCallingEnum(String functionName, String description) {
        this.functionName = functionName;
        this.description = description;
    }


}
