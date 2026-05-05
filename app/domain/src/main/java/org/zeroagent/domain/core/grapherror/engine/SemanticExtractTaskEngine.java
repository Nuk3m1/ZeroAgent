package org.zeroagent.domain.core.grapherror.engine;

import org.jetbrains.annotations.NotNull;
import org.zeroagent.domain.core.card.model.CardInformation;

import java.util.List;

/**
 * 语义关系抽取任务引擎
 * @author Nuk3m1
 * @version 2026年04月23日  15时09分
 */
public interface SemanticExtractTaskEngine {
    /**
     * 执行语义关系抽取
     * @param cardInformationList 卡牌任务批次
     */
    void execute(@NotNull List<CardInformation> cardInformationList);
}
