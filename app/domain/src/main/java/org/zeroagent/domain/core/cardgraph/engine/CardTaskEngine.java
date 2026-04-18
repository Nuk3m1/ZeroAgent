package org.zeroagent.domain.core.cardgraph.engine;

import org.jetbrains.annotations.NotNull;
import org.zeroagent.domain.core.card.model.CardInformation;

import java.util.List;

/**
 * 卡牌任务引擎
 * @author Nuk3m1
 * @version 2026年04月12日  23时02分
 */
public interface CardTaskEngine {
    /**
     * 卡牌任务引擎
     * @param cardInformationList 卡牌信息
     */
    void execute(@NotNull List<CardInformation> cardInformationList);
}
