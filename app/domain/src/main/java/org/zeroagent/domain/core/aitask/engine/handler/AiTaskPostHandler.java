package org.zeroagent.domain.core.aitask.engine.handler;

import org.zeroagent.domain.core.aitask.engine.handler.forward.post.TaskPostAction;
import org.zeroagent.domain.core.aitask.model.AiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时33分
 */
public interface AiTaskPostHandler {
    TaskPostAction postHandle(AiTask aiTask);
}
