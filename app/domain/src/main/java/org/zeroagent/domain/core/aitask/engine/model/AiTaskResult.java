package org.zeroagent.domain.core.aitask.engine.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * 系统标准输出结果
 * @author Nuk3m1
 * @version 2026年05月02日  18时06分
 */
@Data
@Accessors(chain = true)
public class AiTaskResult {
    /**
     * 任务实际耗时
     */
    @Nullable
    private Duration finishedDuration;
    /**
     * 任务取消原因
     */
    @Nullable
    private String   cancelReason;

}
