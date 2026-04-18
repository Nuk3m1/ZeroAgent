package org.zeroagent.infra.dal.adaptor.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jooq.JSONB;
import org.mapstruct.Mapper;
import org.zeroagent.common.mapper.BaseMapperConfig;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.message.Message;
import org.zeroagent.domain.core.ai.chat.model.message.MessageType;
import org.zeroagent.domain.core.ai.chat.model.ConversationMessage;
import org.zeroagent.infra.dal.common.ModelMapper;
import org.zeroagent.infra.dal.common.UpdatableBuilder;
import org.zeroagent.infra.dal.tables.pojos.ConversationMessageCreation;
import org.zeroagent.infra.dal.tables.records.ConversationMessageCreationRecord;

import java.util.List;

import static org.zeroagent.infra.dal.Tables.CONVERSATION_MESSAGE_CREATION;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月08日  23时08分
 */
@Mapper(config = BaseMapperConfig.class)
public interface ConversationMessagePojoMapper extends ModelMapper<ConversationMessage, ConversationMessageCreation, ConversationMessageCreationRecord> {

    @Override
    ConversationMessageCreation toEntity(ConversationMessage conversationMessage);
    @Override
    ConversationMessage toModel(ConversationMessageCreation conversationmessageCreation);

    default MessageType jsonbToMessageType(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseObject(jsonb.data(), MessageType.class);
    }
    default JSONB messageTypeToJsonb(MessageType messageType) {
        if (messageType == null) {
            return null;
        }
        return JSONB.jsonb(JSON.toJSONString(messageType));
    }

    default List<Media> jsonbToMessageContent(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseObject(jsonb.data(), new TypeReference<List<Media>>() {});
    }
    default JSONB messageContentToJsonb(List<Media> content) {
        if (content == null) {
            return null;
        }
        return JSONB.jsonb(JSON.toJSONString(content));
    }
    @Override
    default void updatable(UpdatableBuilder<ConversationMessageCreationRecord> builder) {
        builder.updatable(CONVERSATION_MESSAGE_CREATION.CONTENT);
        builder.updatable(CONVERSATION_MESSAGE_CREATION.REASONING_CONTENT);
    }
}
