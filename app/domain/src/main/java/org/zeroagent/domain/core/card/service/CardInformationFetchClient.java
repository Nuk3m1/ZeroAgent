package org.zeroagent.domain.core.card.service;

import org.zeroagent.domain.core.card.model.CardInformation;

/**
 * 卡牌信息捞取服务 - 具体API实现由infra决定
 * @author Nuk3m1
 * @version 2026年03月20日  11时45分
 */
public interface CardInformationFetchClient {
    /**
     * 捞取所有卡牌
     */
    void getCards();
    /**
     * 根据卡密获取单个卡牌信息
     */
    CardInformation getCardInformationByCardId(String cardId);

}
