package org.zeroagent.infra.dal.adaptor.card;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jooq.JSONB;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.zeroagent.common.mapper.BaseMapperConfig;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;
import org.zeroagent.domain.core.card.model.CardSubTypeEnum;
import org.zeroagent.domain.core.card.model.CardTypeEnum;
import org.zeroagent.infra.dal.common.ModelMapper;
import org.zeroagent.infra.dal.common.UpdatableBuilder;
import org.zeroagent.infra.dal.tables.pojos.CardInformationCreation;
import org.zeroagent.infra.dal.tables.records.CardInformationCreationRecord;

import java.util.List;

import static org.zeroagent.infra.dal.Tables.CARD_INFORMATION_CREATION;


/**
 *
 * @author Nuk3m1
 * @version 2026年03月17日  20时15分
 */
@Mapper(config = BaseMapperConfig.class)
public interface CardInformationPojoMapper extends ModelMapper<CardInformation, CardInformationCreation, CardInformationCreationRecord> {
    @Override
    CardInformationCreation toEntity(CardInformation cardInformation);
    @Override
    CardInformation toModel(CardInformationCreation cardInformation);

    default List<CardSubTypeEnum> jsonbToCardSubtype(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseObject(jsonb.data(), new TypeReference<>() {
        });
    }
    default JSONB cardSubtypeToJsonb(List<CardSubTypeEnum> cardSubtype) {
        if (cardSubtype == null) {
            return null;
        }
        return JSONB.jsonb(JSON.toJSONString(cardSubtype));
    }

    default String toCardType(CardTypeEnum cardType) {
        if (cardType == null) {
            return null;
        }
        return cardType.name();
    }
    default CardTypeEnum fromCardType(String cardType) {
        if (cardType == null) {
            return null;
        }
        String upperCaseType = cardType.toUpperCase();
        for (CardTypeEnum type : CardTypeEnum.values()) {
            if (type.name().equals(upperCaseType)) {
                return type;
            }
        }
        return null;
    }

    default List<String> jsonbToArchetype(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseObject(jsonb.data(), new TypeReference<>() {
        });
    }
    default JSONB archetypeToJsonb(List<String> archetype) {
        if (archetype == null) {
            return null;
        }
        return JSONB.jsonb(JSON.toJSONString(archetype));
    }

    default JSONObject jsonbToBizResponse(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseJSONObject(jsonb.data());
    }
    default JSONB bizResponseToJsonb(JSONObject bizResponse) {
        if (bizResponse == null) {
            return null;
        }
        return JSONB.jsonb(bizResponse.toString());
    }

    default CardInformationStatusEnum graphSyncStatusToCardInformationStatusEnum(int graphSyncStatus) {
        if (graphSyncStatus == 0) {
            return null;
        }
        switch (graphSyncStatus) {
            case 0:
                return CardInformationStatusEnum.PENDING;
            case 1:
                return CardInformationStatusEnum.COMPLETED;
            case 2:
                return CardInformationStatusEnum.SUCCESS;
            case 3:
                return CardInformationStatusEnum.FAILURE;
            case 4:
                return CardInformationStatusEnum.EXECUTING;

            default:
                throw new IllegalArgumentException("Invalid graph sync status: " + graphSyncStatus);
        }
    }
    default int cardInformationStatusToGraphSyncStatus(CardInformationStatusEnum cardInformationStatus) {
        if (cardInformationStatus == null) {
            return 0;
        }
        return cardInformationStatus.getStatus();
    }

    @Override
    default void updatable(UpdatableBuilder<CardInformationCreationRecord> builder) {
        // 除了ID 两个时间戳 以外，均需要修改
        builder.updatable(CARD_INFORMATION_CREATION.EFFECT);
        builder.updatable(CARD_INFORMATION_CREATION.CARD_SUBTYPE);
        builder.updatable(CARD_INFORMATION_CREATION.CARD_TYPE);
        builder.updatable(CARD_INFORMATION_CREATION.ATK);
        builder.updatable(CARD_INFORMATION_CREATION.DEF);
        builder.updatable(CARD_INFORMATION_CREATION.ATTRIBUTION);
        builder.updatable(CARD_INFORMATION_CREATION.LINK_RATING);
        builder.updatable(CARD_INFORMATION_CREATION.MONSTER_LEVEL);
        builder.updatable(CARD_INFORMATION_CREATION.MONSTER_RANK);
        builder.updatable(CARD_INFORMATION_CREATION.NAME);
        builder.updatable(CARD_INFORMATION_CREATION.PENDULUM_EFFECT);
        builder.updatable(CARD_INFORMATION_CREATION.PASSCODE);
        builder.updatable(CARD_INFORMATION_CREATION.RACE);
        builder.updatable(CARD_INFORMATION_CREATION.PENDULUM_SCALE);
        builder.updatable(CARD_INFORMATION_CREATION.GRAPH_SYNC_STATUS);
        builder.updatable(CARD_INFORMATION_CREATION.BIZ_RESPONSE);
        builder.updatable(CARD_INFORMATION_CREATION.ARCHETYPE);
    }

}
