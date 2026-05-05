package org.zeroagent.domain.core.aitask.engine;

import org.jetbrains.annotations.NotNull;
import org.zeroagent.domain.core.aitask.model.AiTask;

/**
 * AI任务引擎
 * @author Nuk3m1
 * @version 2026年04月29日  15时37分
 */
public interface AiTaskEngine {
    /**
     * AI创作任务执行
     * @param aiTask AI创作任务
     */
    void execute(@NotNull AiTask aiTask);
}
