package org.zeroagent.domain.core.ai.chat.model.message;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;

import java.util.List;
import java.util.Map;

/**
 * @author Nuk3m1
 * @version 2026年03月07日  14时35分
 * @Description:
 */
public class UserMessage extends AbstractMessage {


    public UserMessage(@Nullable MediaType mediaType, @Nullable String mediaContent) {
        this(mediaType, mediaContent, Map.of());
    }
    private UserMessage(@Nullable MediaType mediaType, @Nullable String mediaContent, Map<String, Object> metadata) {
        super(MessageType.USER, mediaType, mediaContent, metadata);
    }
    public UserMessage(List<Media> mediaList) {
        super(MessageType.USER, mediaList, Map.of());
    }
}
