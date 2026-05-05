package org.zeroagent.domain.core.aitask.engine.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.zeroagent.common.enums.ICode;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;

import java.time.Duration;

/**
 * 分片消息 - 用于隔离任务及分配处理器
 * @author Nuk3m1
 * @version 2026年04月30日  13时55分
 */
@Data
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "of")
@EqualsAndHashCode(of = {"taskType", "bizType"})
public class AiTaskConfig {
    /**
     * 任务类型
     */
    private final AiTaskType taskType;
    /**
     * 关联外部业务类型
     */
    private String bizType;
    /**
     * 每次调度捞取数量
     */
    private Integer loadSize             = 10;
    /**
     * 锁失效时间
     */
    private Duration execLockTimeOut = Duration.ofSeconds(3);
    /**
     * 最大回滚次数 (到达最大回滚次数后直接取消任务)
     */
    private Integer maxRollbackCount = 3;
    /**
     * 最大执行次数 (超过最大执行次数直接取消)
     */
    private Integer execTryLimit = 3;

    /**
     * 是否要在完成时通知用户
     */
    private boolean notifyUserOnCompleted = false;

    public AiTaskConfig setBizType(String bizType) {
        this.bizType = bizType;
        return this;
    }
    public AiTaskConfig setBizType(ICode bizType) {
        this.bizType = bizType.getCode();
        return this;
    }


}
