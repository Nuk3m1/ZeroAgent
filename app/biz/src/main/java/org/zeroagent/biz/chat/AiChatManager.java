package org.zeroagent.biz.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zeroagent.domain.core.ai.chat.model.Conversation;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;
import org.zeroagent.domain.core.ai.chat.model.message.UserMessage;
import org.zeroagent.domain.core.ai.chat.model.request.ChatMessageMediaVO;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import org.zeroagent.domain.core.ai.chat.model.request.AiChatRequestVO;
import org.zeroagent.domain.core.ai.chat.service.AiChatService;
import org.zeroagent.domain.core.ai.chat.service.ConversationRepository;
import org.zeroagent.domain.core.utils.IdCryptoUtil;
import reactor.core.publisher.Flux;


/**
 * @author Nuk3m1
 * @version 2026年03月05日  16时58分
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatManager {
    private final AiChatService         aiChatService;
    private final ConversationRepository conversationRepository;



    public Flux<MessageChunk> chat(AiChatRequestVO request) {
        final Conversation conversation;
        if (request.getConversationId() != null) {
            long conversationId = IdCryptoUtil.decrypt(request.getConversationId());
            conversation = new Conversation().setId(conversationId);
        } else {
            conversation = new Conversation().setTitle("新会话");
        }
        String userInput = request.getMessages().getFirst().getContent();
        // 适配 图片理解 需要修改 UserMessage 构建逻辑
        UserMessage userMessage = new UserMessage(MediaType.TEXT, userInput);


        // 传递给下游 1. 会话      2. 用户消息（包含文本，拓展图片视频）
        return aiChatService.DouBaoChatStream(conversation, userMessage, userInput);
    }


}
