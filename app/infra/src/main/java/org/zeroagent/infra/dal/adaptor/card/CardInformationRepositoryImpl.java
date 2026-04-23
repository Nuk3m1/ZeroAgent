package org.zeroagent.infra.dal.adaptor.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationQO;
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
@Slf4j
@RequiredArgsConstructor
public class CardInformationRepositoryImpl implements CardInformationRepository {
    private final DSLContext                      dsl;
    private final CardInformationPojoMapper       cardInformationPojoMapper;
    private final CardInformationCreationDao      cardInformationCreationDao;
    private final ObjectMapper                    objectMapper;

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
    public Optional<CardInformation> findByPassCode(String passCode) {
        return cardInformationCreationDao.fetchOptionalByPasscode(passCode).map(cardInformationPojoMapper::toModel);
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

    @Override
    public List<CardInformation> fetchBatchByCondition(CardInformationQO cardInformationQO) {
        Assert.notNull(cardInformationQO, "cardInformationQO must not be null");
        Condition condition = noCondition();
        try {
            if (cardInformationQO.getRace() != null && !cardInformationQO.getRace().isEmpty()) {
                condition = condition.and(CARD_INFORMATION_CREATION.RACE.eq(cardInformationQO.getRace()));
            }
            if (cardInformationQO.getAttribute() != null && !cardInformationQO.getAttribute().isEmpty()) {
                condition = condition.and(CARD_INFORMATION_CREATION.ATTRIBUTION.eq(cardInformationQO.getAttribute()));
            }
            if (cardInformationQO.getMainType() != null && !cardInformationQO.getMainType().isEmpty()) {
                condition = condition.and(CARD_INFORMATION_CREATION.CARD_TYPE.eq(cardInformationQO.getMainType()));
            }

            // 处理字段，直接从卡名进行模糊匹配
            if (cardInformationQO.getArchetypes() != null && !cardInformationQO.getArchetypes().isEmpty()) {
                Condition archeTypeCondition = cardInformationQO.getArchetypes().stream()
                        .map(archetype -> (Condition) CARD_INFORMATION_CREATION.NAME.like("%" + archetype + "%"))
                        .reduce(Condition::or)
                        .orElse(DSL.noCondition());
                condition = condition.and(archeTypeCondition);
            }
            // 处理jsonb数组的包含关系 (@>)
            if (cardInformationQO.getSubTypes() != null && !cardInformationQO.getSubTypes().isEmpty()) {
                String subTypesJson = objectMapper.writeValueAsString(cardInformationQO.getSubTypes());
                condition = condition.and(DSL.condition("{0} @> {1}::jsonb", CARD_INFORMATION_CREATION.CARD_SUBTYPE, DSL.val(subTypesJson)));
            }
            // 星级为 Short 类型，进行特殊处理
            Short levelValue = cardInformationQO.getLevel() != null ? cardInformationQO.getLevel().shortValue() : null;
            // 添加 星级 攻击力 防御力 的筛选条件
            condition = appendNumericCondition(condition, CARD_INFORMATION_CREATION.MONSTER_LEVEL, cardInformationQO.getLevelOperator(), levelValue);
            condition = appendNumericCondition(condition, CARD_INFORMATION_CREATION.ATK, cardInformationQO.getAtkOperator(), cardInformationQO.getAtk());
            condition = appendNumericCondition(condition, CARD_INFORMATION_CREATION.DEF, cardInformationQO.getDefOperator(), cardInformationQO.getDef());

            return dsl.selectFrom(CARD_INFORMATION_CREATION)
                    .where(condition)
                    .fetchInto(CardInformationCreation.class)
                    .stream().map(cardInformationPojoMapper::toModel)
                    .toList();
        } catch (JsonProcessingException e) {
            log.error("JSONB参数序列化失败，入参: {}", cardInformationQO, e);
            throw new RuntimeException("工具调用参数解析失败", e);
        } catch (Exception e) {
            log.error("卡牌图谱关系提取查询失败，条件: {}", condition, e);
            throw new RuntimeException("卡牌图谱关系提取 - 数据库查询环节出错", e);
        }

    }



    private <T extends Number> Condition appendNumericCondition(Condition currentCondition,
                                             Field<T> field,
                                             String operator,
                                             T value) {
        if (value == null || !StringUtils.hasText(operator)) {
            return currentCondition;
        }
        return switch (operator) {
            case "=" -> currentCondition.and(field.eq(value));
            case ">" -> currentCondition.and(field.gt(value));
            case ">=" -> currentCondition.and(field.ge(value));
            case "<" -> currentCondition.and(field.lt(value));
            case "<=" -> currentCondition.and(field.le(value));
            default -> {
                log.error("出现异常数值操作符: {}", operator);
                yield currentCondition;
            }
        };
    }
}
