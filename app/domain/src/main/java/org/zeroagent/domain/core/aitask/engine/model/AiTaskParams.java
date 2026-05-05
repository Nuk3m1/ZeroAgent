package org.zeroagent.domain.core.aitask.engine.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * 系统标准输入参数
 * @author Nuk3m1
 * @version 2026年05月02日  18时05分
 */
@Data
@Accessors(chain = true)
public class AiTaskParams {
    /**
     * 任务超时时间（从任务创建时间开始算，如果执行中检测到超时，则会强制取消任务）
     */
    @Nullable
    private Duration timeoutFromCreatedAt;
    /**
     * 任务超时时间（从任务启动时间开始算，如果执行中检测到超时，则会强制取消任务）
     */
    @Nullable
    private Duration timeoutFromStartedAt;
}
