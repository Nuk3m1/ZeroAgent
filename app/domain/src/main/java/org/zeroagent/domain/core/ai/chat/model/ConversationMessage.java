package org.zeroagent.domain.core.ai.chat.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.zeroagent.common.id.AutoIncrement;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.message.MessageType;

import java.time.ZonedDateTime;
import java.util.List;

/**
 *
 *  会话消息 - 领域实体模型
 * @author Nuk3m1
 * @version 2026年03月08日  23时09分
 */
@Data
@Accessors(chain=true)
public class ConversationMessage {
    @AutoIncrement
    private long id;
    /**
     * 创建时间
     */
    private ZonedDateTime createdAt;
    /**
     * 更新时间
     */

    private ZonedDateTime  updatedAt;
    /**
     * 用户ID
     */
    private Long           uid;
    /**
     * 会话ID
     */
    private Long           conversationId;
    /**
     *  角色
     */
    private MessageType role;
    /**
     *  内容
     */
    private List<Media> content;
    /**
     * 深度思考内容
     */
    private String reasoningContent;
}
