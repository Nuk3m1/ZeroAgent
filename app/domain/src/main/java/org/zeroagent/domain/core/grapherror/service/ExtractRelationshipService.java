package org.zeroagent.domain.core.grapherror.service;

import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;

/**
 * 用于分离关系抽取的Agent对话逻辑 - 工具调用参数处理
 * @author Nuk3m1
 * @version 2026年04月21日  20时45分
 */

public interface ExtractRelationshipService {
    void extractCardRules(String sourceCardId,String sourceCardName, String effect, ToolCallingEnum toolCallingEnum);
}
