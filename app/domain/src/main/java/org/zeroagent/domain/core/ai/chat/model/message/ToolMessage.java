package org.zeroagent.domain.core.ai.chat.model.message;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.ai.chat.model.media.Media;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月15日  14时23分
 */
public class ToolMessage extends AbstractMessage{


    public ToolMessage(@Nullable MediaType mediaType, @Nullable String mediaContent) {
        this(mediaType, mediaContent, Map.of());
    }

    public ToolMessage(@Nullable MediaType mediaType, @Nullable String mediaContent, Map<String, Object> metadata) {
        super(MessageType.TOOL, mediaType, mediaContent, metadata);
    }

    public ToolMessage(List<Media> mediaList) {
        super(MessageType.TOOL, mediaList, Map.of());
    }
}
