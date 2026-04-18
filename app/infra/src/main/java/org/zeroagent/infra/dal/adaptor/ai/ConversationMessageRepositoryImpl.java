package org.zeroagent.infra.dal.adaptor.ai;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.zeroagent.domain.core.ai.chat.model.ConversationMessage;
import org.zeroagent.domain.core.ai.chat.service.ConversationMessageRepository;
import org.zeroagent.infra.dal.tables.daos.ConversationMessageCreationDao;
import org.zeroagent.infra.dal.tables.pojos.ConversationMessageCreation;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author Nuk3m1
 * @version 2026年03月09日  15时20分
 */
@Repository
@RequiredArgsConstructor
public class ConversationMessageRepositoryImpl implements ConversationMessageRepository {
    private final DSLContext                        dsl;
    private final ConversationMessageCreationDao    conversationMessagecreationDao;
    private final ConversationMessagePojoMapper     conversationMessagePojoMapper;


    @Override
    public long create(ConversationMessage conversationMessage) {
        ConversationMessageCreation entity = conversationMessagePojoMapper.toEntity(conversationMessage);
        conversationMessagecreationDao.insert(entity);
        return Objects.requireNonNull(entity.getId());
    }

    @Override
    public List<ConversationMessage> queryByConversationId(long conversationId) {
        return conversationMessagecreationDao.fetchByConversationId(conversationId)
                .stream()
                .map(conversationMessagePojoMapper::toModel)
                .collect(toList());
    }

}
