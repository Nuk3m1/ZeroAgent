package org.zeroagent.infra.dal.adaptor.ai;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.zeroagent.domain.core.ai.chat.model.Conversation;
import org.zeroagent.domain.core.ai.chat.service.ConversationRepository;
import org.zeroagent.infra.dal.tables.daos.ConversationCreationDao;
import org.zeroagent.infra.dal.tables.pojos.ConversationCreation;
import org.zeroagent.infra.dal.tables.records.ConversationCreationRecord;

import java.util.Objects;
import java.util.Optional;

import static org.zeroagent.infra.dal.Tables.CONVERSATION_CREATION;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月09日  15时52分
 */
@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {
    private final DSLContext                 dsl;
    private final ConversationPojoMapper     conversationPojoMapper;
    private final ConversationCreationDao    conversationCreationDao;

    @Override
    public long create(Conversation conversation) {
        ConversationCreation entity = conversationPojoMapper.toEntity(conversation);
        conversationCreationDao.insert(entity);
        return Objects.requireNonNull(entity.getId());
    }

    @Override
    public void updateById(Conversation conversation) {
        ConversationCreationRecord updatingRecord = conversationPojoMapper.toUpdatingRecord(conversation);
        if (! updatingRecord.changed()) {
            return;
        }
        dsl.update(CONVERSATION_CREATION)
                .set(updatingRecord)
                .where(CONVERSATION_CREATION.ID.eq(conversation.getId()))
                .execute();

    }

    @Override
    public Optional<Conversation> queryOptionalById(long id) {
        return conversationCreationDao.fetchOptionalById(id).map(conversationPojoMapper::toModel);
    }
}
