package org.zeroagent.domain.core.card.service;

import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;

import java.util.List;
import java.util.Optional;

/**
 * ORM框架 - 实现CardInformation领域对象持久化服务
 */
public interface CardInformationRepository {
    long create(CardInformation cardInformation);
    void batchInsertIfNotExists(List<CardInformation> cardInformations);
    void batchCreate(List<CardInformation> cardInformations);
    void updateById(CardInformation cardInformation);
    Optional<CardInformation> findById(long id);
    List<CardInformation> fetchBatchByStatus(int limit, CardInformationStatusEnum status);
    void updateExecuteStatusById(long id, CardInformationStatusEnum status);

    CardInformation getCardByFuzzyName(String fuzzyName);
}
