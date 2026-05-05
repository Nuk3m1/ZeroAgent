package org.zeroagent.domain.core.aitask.service;

import org.jetbrains.annotations.NotNull;
import org.zeroagent.domain.core.aitask.model.AiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年05月04日  21时47分
 */
public interface AiTaskService {

    /**
     * 提交一条新任务 (幂等)
     * @param aiTask 待提交任务
     * @return       任务ID
     */
    long submit(@NotNull AiTask aiTask);
}
