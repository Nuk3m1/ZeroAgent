package org.zeroagent.domain.core.ai.chat.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;

import java.util.List;

/**
 *  AI对话上下文 - 实体模型
 * @author Nuk3m1
 * @version 2026年03月10日  15时56分
 */
@Data
@Accessors(chain = true)
public class ConversationContext {
    private Long                uid;
    /**
     * 会话ID
     */
    private Long                conversationId;
    /**
     * 是否为初次对话
     */
    private boolean             isFirstRound;
    /**
     * 本次用户输入 （尚未持久化）
     */
    private List<Media>              userInput;
    /**
     * 流式响应碎片集合
     */
    private List<MessageChunk>  messageChunks;
    /**
     *  调用是否成功
     */
    private boolean             success;
    /**
     *  错误信息
     */
    private String              error;

    public void setError(Throwable throwable) {
        this.error = throwable.getMessage();
    }
}
