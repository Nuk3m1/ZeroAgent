package org.zeroagent.infra.dal.adaptor.card;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.infra.dal.tables.daos.CardInformationCreationDao;
import org.zeroagent.infra.dal.tables.pojos.CardInformationCreation;
import org.zeroagent.infra.dal.tables.records.CardInformationCreationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jooq.impl.DSL.*;
import static org.zeroagent.infra.dal.Tables.CARD_INFORMATION_CREATION;


/**
 *
 * @author Nuk3m1
 * @version 2026年03月17日  20时34分
 */
@Repository
@RequiredArgsConstructor
public class CardInformationRepositoryImpl implements CardInformationRepository {
    private final DSLContext                      dsl;
    private final CardInformationPojoMapper       cardInformationPojoMapper;
    private final CardInformationCreationDao      cardInformationCreationDao;


    @Override
    public long create(CardInformation cardInformation) {
        if (cardInformation.getId() == null) {
            cardInformation.setId(IdHelper.getId());
        }
        CardInformationCreation entity = cardInformationPojoMapper.toEntity(cardInformation);
        cardInformationCreationDao.insert(entity);
        return Objects.requireNonNull(entity.getId());
    }

    @Override
    public void batchInsertIfNotExists(List<CardInformation> cardInformations) {
        List<CardInformationCreation> cardInformationCreations = cardInformationPojoMapper.toEntities(cardInformations);
        dsl.insertInto(CARD_INFORMATION_CREATION)
                .values(cardInformationCreations)
                .onConflict(CARD_INFORMATION_CREATION.PASSCODE)
                .doNothing()
                .execute();
    }

    @Override
    public void batchCreate(List<CardInformation> cardInformations) {
        List<CardInformationCreation> cardInformationCreations = cardInformationPojoMapper.toEntities(cardInformations);
        cardInformationCreationDao.insert(cardInformationCreations);
    }

    @Override
    public void updateById(CardInformation cardInformation) {
        CardInformationCreationRecord updatingRecord = cardInformationPojoMapper.toUpdatingRecord(cardInformation);
        if (! updatingRecord.changed()) {
            return;
        }
        dsl.update(CARD_INFORMATION_CREATION)
                .set(updatingRecord)
                .where(CARD_INFORMATION_CREATION.ID.eq(cardInformation.getId()))
                .execute();
    }

    @Override
    public Optional<CardInformation> findById(long id) {
        return cardInformationCreationDao.fetchOptionalById(id).map(cardInformationPojoMapper::toModel);
    }

    @Override
    public List<CardInformation> fetchBatchByStatus(int limit, CardInformationStatusEnum status) {
        return dsl.selectFrom(CARD_INFORMATION_CREATION)
                .where(CARD_INFORMATION_CREATION.GRAPH_SYNC_STATUS.eq((short) status.getStatus()))
                .orderBy(CARD_INFORMATION_CREATION.ID.asc())
                .limit(limit)
                .forUpdate()
                .skipLocked()
                .fetchInto(CardInformationCreation.class)
                .stream().map(cardInformationPojoMapper::toModel)
                .toList();

    }

    @Override
    public void updateExecuteStatusById(long id, CardInformationStatusEnum status) {
        dsl.update(CARD_INFORMATION_CREATION)
                .set(CARD_INFORMATION_CREATION.GRAPH_SYNC_STATUS, (short) status.getStatus())
                .where(CARD_INFORMATION_CREATION.ID.eq(id))
                .execute();
    }

    /**
     * pg_trgm 进行卡名模糊匹配
     * @param fuzzyName 模糊卡名
     * @return 最高相似度记录
     */
    @Override
    public CardInformation getCardByFuzzyName(String fuzzyName) {
        return dsl.selectFrom(CARD_INFORMATION_CREATION)
                .where(condition("{0} % {1}", CARD_INFORMATION_CREATION.NAME, val(fuzzyName)))
                .orderBy(field("similarity({0}, {1})",
                        Double.class, CARD_INFORMATION_CREATION.NAME,
                        val(fuzzyName)
                ).desc())
                .limit(1)
                .fetchOptionalInto(CardInformationCreation.class)
                .map(cardInformationPojoMapper::toModel).orElse(null);
    }
}
