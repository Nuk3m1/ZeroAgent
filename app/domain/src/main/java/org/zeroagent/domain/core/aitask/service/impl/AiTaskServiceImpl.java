package org.zeroagent.domain.core.aitask.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.zeroagent.domain.core.aitask.engine.handler.factory.AiTaskHandlerFactory;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.service.AiTaskRepository;
import org.zeroagent.domain.core.aitask.service.AiTaskService;

/**
 *
 * @author Nuk3m1
 * @version 2026年05月04日  21时48分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl implements AiTaskService {
    private final AiTaskRepository      aiTaskRepository;
    private final AiTaskHandlerFactory  aiTaskHandlerFactory;


    @Override
    public long submit(@NotNull AiTask aiTask) {
        long id = aiTaskRepository.createIdempotent(aiTask).getId();
        if (aiTaskHandlerFactory.getHandler(aiTask.getTaskType(), aiTask.getBizType()) == null) {
            log.warn("未找到对应的任务处理器，taskId = {}", aiTask.getId());
        }
        // TODO 对于特殊类型的任务可以在这里做提交后的通知 "[收到一条新任务]"
        return id;
    }
}
