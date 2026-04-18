package org.zeroagent.domain.core.ai.chat.model.message;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;

import java.util.List;
import java.util.Map;

/**
 * @author Nuk3m1
 * @version 2026年03月07日  16时25分
 * @Description:
 */
public class SystemMessage extends AbstractMessage {


    public SystemMessage(@Nullable MediaType mediaType, @Nullable String mediaContent) {
        this(mediaType, mediaContent, Map.of());
    }
    private SystemMessage(@Nullable MediaType mediaType, @Nullable String mediaContent, Map<String, Object> metadata) {
        super(MessageType.SYSTEM, mediaType, mediaContent, metadata);
    }
    public SystemMessage(List<Media> mediaList) {
        super(MessageType.SYSTEM, mediaList, Map.of());
    }
}
