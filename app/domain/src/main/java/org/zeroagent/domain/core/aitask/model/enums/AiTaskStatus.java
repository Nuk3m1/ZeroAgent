package org.zeroagent.domain.core.aitask.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月26日  22时55分
 */
@Getter
@RequiredArgsConstructor
public enum AiTaskStatus {
    /*--  起始态 --*/
    CREATED("已创建"),
    /*--  运行态 --*/
    RUNNING("运行中"),
    /*--  最终态 --*/
    FINISHED("已完成"),
    CANCELED("已取消"),
    DELETED("已删除")
    ;
    private final String desc;

    /**
     * 获取非删除的所有状态
     * @return ~
     */
    public static List<String> notDeleted() {
        return List.of(CREATED.name(),  RUNNING.name(), FINISHED.name(), CANCELED.name() );
    }
    /**
     * 是否为最终态
     */
    public boolean isFinal() {
        return this == FINISHED || this == CANCELED;
    }
    /**
     *  是否已取消
     */
    public boolean isCanceled() {
        return this == CANCELED;
    }
    /**
     * 是否已完成
     */
    public boolean isFinished() {
        return this == FINISHED;
    }
}
