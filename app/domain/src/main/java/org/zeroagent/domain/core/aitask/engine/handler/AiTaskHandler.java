package org.zeroagent.domain.core.aitask.engine.handler;

import org.zeroagent.domain.core.aitask.engine.handler.forward.TaskAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.post.TaskPostAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.pre.TaskPreAction;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskConfig;

/**
 * 任务处理器 顶级接口
 * @author Nuk3m1
 * @version 2026年04月29日  15时40分
 */
public interface AiTaskHandler extends  AiTaskPreHandler, AiTaskPostHandler {
    AiTaskConfig config();

    @Override
    TaskPreAction preHandle(AiTask aiTask);

    TaskAction handle(AiTask aiTask);

    @Override
    TaskPostAction postHandle(AiTask aiTask);
}
