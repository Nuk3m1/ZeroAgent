package org.zeroagent.domain.core.aitask.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月26日  22时53分
 */
@Getter
@RequiredArgsConstructor
public enum AiTaskType {
    AI_IMAGE("AI生图"),
    Ai_VIDEO("AI生视频")
    ;
    private final String desc;

}
