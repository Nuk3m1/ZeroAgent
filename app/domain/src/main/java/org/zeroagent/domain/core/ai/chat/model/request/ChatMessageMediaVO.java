package org.zeroagent.domain.core.ai.chat.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  17时13分
 * @Description: 单条消息
 */
@Data
public class ChatMessageMediaVO {
    @NotNull
    private String role;
    @NotBlank
    private String      content;
    // TODO 后续拓展图片视频类型 FileRef
}
