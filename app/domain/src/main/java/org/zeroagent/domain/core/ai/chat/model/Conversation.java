package org.zeroagent.domain.core.ai.chat.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月09日  15时02分
 */
@Data
@Accessors(chain = true)
public class Conversation {

    /**
     * 主键ID
     */
    private Long          id;
    /**
     * 创建时间
     */
    private ZonedDateTime createdAt;
    /**
     * 更新时间
     */
    private ZonedDateTime updatedAt;
    /**
     * 用户ID
     */
    private Long          uid;
    /**
     * 标题
     */
    private String        title;


}

