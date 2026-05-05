package org.zeroagent.domain.core.aitask.engine.handler;

import org.zeroagent.domain.core.aitask.engine.handler.forward.post.TaskPostAction;
import org.zeroagent.domain.core.aitask.engine.handler.forward.pre.TaskPreAction;
import org.zeroagent.domain.core.aitask.model.AiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时35分
 */
public interface SimpleTaskHandler extends AiTaskHandler {
    @Override
    default TaskPreAction preHandle(AiTask aiTask) {
        return TaskPreAction.FORWARD_RUNNING;
    }
    @Override
    default TaskPostAction postHandle(AiTask aiTask) {
        return TaskPostAction.COMPLETE;
    }
}
