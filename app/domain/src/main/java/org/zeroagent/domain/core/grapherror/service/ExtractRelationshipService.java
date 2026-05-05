package org.zeroagent.domain.core.grapherror.service;

import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.cardgraph.model.GraphRelationTypeEnum;
import reactor.core.publisher.Mono;

/**
 * 用于分离关系抽取的Agent对话逻辑 - 工具调用参数处理
 * @author Nuk3m1
 * @version 2026年04月21日  20时45分
 */

public interface ExtractRelationshipService {
    /**
     * 执行关系抽取并在完成后发出完成信号，错误通过 Mono error 传递给调用方。
     */
    Mono<Void> extractCardRules(String sourceCardPassCode, String sourceCardName, String effect, ToolCallingEnum toolCallingEnum, GraphRelationTypeEnum graphRelationTypeEnum);
}
