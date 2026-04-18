package org.zeroagent.domain.core.ai.chat.service;


import org.zeroagent.domain.core.ai.chat.model.ConversationMessage;

import java.util.List;

public interface ConversationMessageRepository {
    long create(ConversationMessage conversationMessage);
    List<ConversationMessage> queryByConversationId(long conversationId);
}
