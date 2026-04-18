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
public class AssistantMessage extends AbstractMessage {

    public AssistantMessage(@Nullable MediaType mediaType, @Nullable String mediaContent) {
        this(mediaType, mediaContent, Map.of());
    }
    private AssistantMessage(@Nullable MediaType mediaType, @Nullable String mediaContent, Map<String, Object> metadata) {
        super(MessageType.ASSISTANT, mediaType, mediaContent, metadata);
    }
    public AssistantMessage(List<Media> mediaList) {
        super(MessageType.ASSISTANT, mediaList, Map.of());
    }
}
