package org.zeroagent.domain.core.ai.chat.model.media;

public enum MediaType {
    TEXT("text"),
    IMAGE("image_url"),
    VIDEO("video_url");
    private final String value;
    MediaType(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
}
