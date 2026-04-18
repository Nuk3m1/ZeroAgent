package org.zeroagent.domain.core.ai.chat.service;

import org.zeroagent.domain.core.ai.chat.model.Conversation;

import java.util.Optional;

public interface ConversationRepository {
    long create(Conversation conversation);

    void updateById(Conversation conversation);

    Optional<Conversation> queryOptionalById(long id);


}
