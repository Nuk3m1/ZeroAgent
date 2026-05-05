package org.zeroagent.domain.core.aitask.engine.model;

import org.zeroagent.domain.core.aitask.engine.handler.AiTaskHandler;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  14时05分
 */
public record AiTaskHandlerContainer(AiTaskConfig aiTaskConfig, AiTaskHandler aiTaskHandler) {
}
