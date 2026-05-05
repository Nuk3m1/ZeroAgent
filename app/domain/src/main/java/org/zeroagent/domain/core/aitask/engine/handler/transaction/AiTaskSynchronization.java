package org.zeroagent.domain.core.aitask.engine.handler.transaction;

/**
 *
 * @author Nuk3m1
 * @version 2026年05月02日  22时59分
 */
@FunctionalInterface
public interface AiTaskSynchronization {
    /**
     *  在任务的更新事务内执行
     */
    void inCommit();
}
