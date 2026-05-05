package org.zeroagent.domain.core.aitask.engine.handler.factory;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.core.aitask.engine.handler.AiTaskHandler;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskConfig;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskHandlerContainer;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时41分
 */
@Component
@RequiredArgsConstructor
public class AiTaskHandlerFactoryImpl implements AiTaskHandlerFactory, ApplicationListener<ApplicationReadyEvent> {
    private static final String CACHE_KEY_PREFIX = "zeroagent" + ":ai_task:";
    private final Map<String, AiTaskHandlerContainer> taskHandlerContainerMap = new HashMap<>();

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        event.getApplicationContext().getBeansOfType(AiTaskHandler.class).values().forEach(aiTaskHandler -> {
            AiTaskConfig config = aiTaskHandler.config();
            this.check(config);
            AiTaskHandlerContainer container = new AiTaskHandlerContainer(config, aiTaskHandler);
            taskHandlerContainerMap.put(this.toCacheKey(config), container);
        });
    }
    private void check(AiTaskConfig aiTaskConfig) {
        Asserts.isTrue(aiTaskConfig.getLoadSize() <= 100, "最大捞取数量必须小于100");
    }

    @Override
    public Set<AiTaskConfig> getAllTaskConfigs() {
        List<AiTaskHandlerContainer> containers = new ArrayList<>(taskHandlerContainerMap.values());
        return containers.stream().map(AiTaskHandlerContainer::aiTaskConfig).collect(Collectors.toSet());
    }

    @Override
    public AiTaskConfig getTaskConfig(AiTaskType aiTaskType, String bizType) {
        AiTaskHandlerContainer container = this.getTaskHandlerContainer(aiTaskType, bizType);
        return container.aiTaskConfig();
    }
    @Nullable
    @Override
    public AiTaskHandler getHandler(AiTaskType aiTaskType, String bizType) {
        AiTaskHandlerContainer container = this.getTaskHandlerContainer(aiTaskType, bizType);
        return container == null ? null : container.aiTaskHandler();
    }

    @Nullable
    private AiTaskHandlerContainer findTaskHandlerContainer(AiTaskType aiTaskType, String bizType) {
        AiTaskHandlerContainer container = taskHandlerContainerMap.get(this.toCacheKey(aiTaskType, bizType));
        if (Objects.isNull(container)) {
            container = taskHandlerContainerMap.get(this.toCacheKey(aiTaskType, null));
        }
        return container;
    }
    private AiTaskHandlerContainer getTaskHandlerContainer(AiTaskType aiTaskType, String bizType) {
        AiTaskHandlerContainer container = this.findTaskHandlerContainer(aiTaskType, bizType);
        Asserts.notNull(container, "task handler not exist");
        return container;
    }


    private String toCacheKey(AiTaskConfig taskConfig) {
        return this.toCacheKey(taskConfig.getTaskType(), taskConfig.getBizType());
    }
    private String toCacheKey(AiTaskType taskType, @Nullable String bizType) {
        if (bizType == null) {
            return CACHE_KEY_PREFIX + taskType;
        } else {
            return CACHE_KEY_PREFIX + taskType + ":" + bizType;
        }
    }
}
