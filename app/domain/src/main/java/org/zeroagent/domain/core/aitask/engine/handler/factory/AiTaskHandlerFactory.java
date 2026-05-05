package org.zeroagent.domain.core.aitask.engine.handler.factory;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.domain.core.aitask.engine.handler.AiTaskHandler;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskConfig;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;

import java.util.Set;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月29日  15时39分
 */
public interface AiTaskHandlerFactory {
    Set<AiTaskConfig> getAllTaskConfigs();

    AiTaskConfig getTaskConfig(AiTaskType aiTaskType, String bizType);
    @Nullable
    AiTaskHandler getHandler(AiTaskType aiTaskType, String bizType);
}
