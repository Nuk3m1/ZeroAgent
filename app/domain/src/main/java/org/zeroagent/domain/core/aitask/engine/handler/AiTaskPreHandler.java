package org.zeroagent.domain.core.aitask.engine.handler;

import org.zeroagent.domain.core.aitask.engine.handler.forward.pre.TaskPreAction;
import org.zeroagent.domain.core.aitask.model.AiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时32分
 */
public interface AiTaskPreHandler {
    TaskPreAction preHandle(AiTask aiTask);
}
