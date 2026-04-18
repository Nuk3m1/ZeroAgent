package org.zeroagent.infra.dal.adaptor.ai;


import org.mapstruct.Mapper;
import org.zeroagent.common.mapper.BaseMapperConfig;
import org.zeroagent.domain.core.ai.chat.model.Conversation;
import org.zeroagent.infra.dal.common.ModelMapper;
import org.zeroagent.infra.dal.common.UpdatableBuilder;
import org.zeroagent.infra.dal.tables.pojos.ConversationCreation;
import org.zeroagent.infra.dal.tables.records.ConversationCreationRecord;

import static org.zeroagent.infra.dal.Tables.CONVERSATION_CREATION;

@Mapper(config = BaseMapperConfig.class)
public interface ConversationPojoMapper extends ModelMapper<Conversation, ConversationCreation, ConversationCreationRecord> {
    @Override
    ConversationCreation toEntity(Conversation conversation);
    @Override
    Conversation toModel(ConversationCreation conversationCreation);

    @Override
    default void updatable(UpdatableBuilder<ConversationCreationRecord> builder) {
        builder.updatable(CONVERSATION_CREATION.TITLE);
    }


}
