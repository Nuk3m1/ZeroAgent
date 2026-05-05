package org.zeroagent.domain.common.async;

import lombok.experimental.UtilityClass;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月09日  15时21分
 */
@UtilityClass
public class AsyncPools {
    public static final String SECURITY_POOL_SUFFIX                        = "$Security";
    /**
     * 捞取卡牌的线程池
     */
    public static final String CARD_SCHEDULER_LOAD_POOL                    = "CARD_SCHEDULER_LOAD_POOL";
    /**
     * 执行卡牌节点创建的线程池
     */
    public static final String CARD_SCHEDULER_EXECUTE_POOL                 = "CARD_SCHEDULER_EXECUTE_POOL";
    /**
     * 语义关系抽取执行线程池
     */
    public static final String SEMANTIC_EXTRACT_EXECUTE_POOL               = "SEMANTIC_EXTRACT_EXECUTE_POOL";
    /**
     * 审批任务执行线程池
     */
    public static final String GRAPH_APPROVAL_EXECUTE_POOL                 = "GRAPH_APPROVAL_EXECUTE_POOL";
    /**
     * AiTask执行线程池
     */
    public static final String AI_TASK_EXECUTE_POOL                         = "AI_TASK_EXECUTE_POOL";
}
