package org.zeroagent.domain.core.ai.chat.service;

import org.zeroagent.domain.core.ai.chat.model.Conversation;
import org.zeroagent.domain.core.ai.chat.model.ConversationContext;
import org.zeroagent.domain.core.ai.chat.model.message.UserMessage;
import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import reactor.core.publisher.Flux;

public interface AiChatService {
    Flux<MessageChunk> DouBaoChatStream(Conversation conversation, UserMessage userMessage, String userInput);

}
