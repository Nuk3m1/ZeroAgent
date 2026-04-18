package org.zeroagent.domain.core.card.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  20时54分
 */
@Getter
@RequiredArgsConstructor
public enum CardInformationStatusEnum {
    /**
     * 待处理
     */
    PENDING(0),
    /**
     * 已完成(完成语义关系搭建)
     */
    COMPLETED(1),
    /**
     * 已同步(完成结构化关系搭建)
     */
    SUCCESS(2),
    /**
     * 异常结果
     */
    FAILURE(3),
    /**
     * 处理中
     */
    EXECUTING(4);



    CardInformationStatusEnum(int status) {
        this.status = status;
    }

    private int status;
}
