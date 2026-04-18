package org.zeroagent.domain.core.grapherror.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.json.JSONObject;

import java.time.ZonedDateTime;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  20时58分
 */
@Data
@Accessors(chain = true)
public class GraphErrorLog {
    /**
     * 主键ID
     */
    private Long                                id;
    /**
     * 创建时间
     */
    private ZonedDateTime                       createdAt;
    /**
     * 更新时间
     */
    private ZonedDateTime                       updatedAt;
    /**
     * 源卡牌ID
     */
    private Long                                sourceCardId;
    /**
     * 源卡牌名
     */
    private String                              sourceCardName;
    /**
     * llm模型原始回复
     */
    private JSONObject                          llmRawResponse;
    /**
     * 错误类型
     */
    private String                              errorType;
    /**
     * 错误信息
     */
    private String                              errorMessage;
    /**
     * 是否修复
     */
    private boolean                             resolved;


}
