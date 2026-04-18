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
}
