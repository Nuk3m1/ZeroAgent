package org.zeroagent.domain.core.ai.chat.model.message;

import lombok.Getter;

@Getter
public enum MessageType {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    TOOL("tool");


    private final String value;
    private MessageType(String value) {
        this.value = value;
    }

    public static MessageType fromValue(String value) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getValue().equals(value)) {
                return messageType;
            }
        }
        return null;
    }

}
