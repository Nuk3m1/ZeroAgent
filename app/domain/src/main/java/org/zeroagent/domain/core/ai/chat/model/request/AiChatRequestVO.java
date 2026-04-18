package org.zeroagent.domain.core.ai.chat.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  17时04分
 * @Description: 前端传入-会话conversation下的请求体：挂载多条消息
 */
@Data
public class AiChatRequestVO {
    @Nullable
    private String              conversationId;
    @NotEmpty
    private List<ChatMessageMediaVO> messages;
}
