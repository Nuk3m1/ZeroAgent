package org.zeroagent.domain.core.ai.chat.model.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;

import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nuk3m1
 * @version 2026年03月07日  14时27分
 * @Description:
 */
public abstract class AbstractMessage implements Message {
    public static final String MESSAGE_TYPE = "messageType";
    // 反序列化 -> 字符串
    @JsonIgnore
    protected final MessageType messageType;

    protected final List<Media> content;

    @JsonIgnore
    protected final Map<String, Object> metadata;


    protected AbstractMessage(MessageType messageType, @Nullable MediaType mediaType, @Nullable String mediaContent, Map<String, Object> metadata) {
        Asserts.notNull(messageType, "Message type must not be null");
        if (messageType == MessageType.SYSTEM || messageType == MessageType.USER) {
            Asserts.notNull(mediaContent, "Content must not be null for SYSTEM or USER messages");
        }
        this.content = new ArrayList<>();
        Asserts.notNull(metadata, "Metadata must not be null");
        this.messageType = messageType;
        this.content.add(new Media(mediaType, mediaContent));
        this.metadata = new HashMap<>(metadata);
        this.metadata.put("role", messageType.getValue());
    }

    protected AbstractMessage(MessageType messageType, List<Media> content, Map<String, Object> metadata) {
        Asserts.notNull(messageType, "Message type must not be null");
        if (messageType == MessageType.SYSTEM || messageType == MessageType.USER) {
            Asserts.notNull(metadata, "Content must not be null for SYSTEM or USER messages");
        }
        this.content = new ArrayList<>();
        this.content.addAll(content);
        Asserts.notNull(metadata, "Metadata must not be null");
        this.messageType = messageType;
        this.metadata = new HashMap<>(metadata);
        this.metadata.put("role", messageType.getValue());
    }

    // 去掉 metadata 的外 key ，直接暴露其中的内容
    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }
    public List<Media> getContent() {
        return this.content;
    }
    public MessageType getMessageType() {
        return this.messageType;
    }
}
