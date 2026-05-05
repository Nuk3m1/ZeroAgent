package org.zeroagent.domain.core.aitask.engine.handler.transaction;

import lombok.experimental.UtilityClass;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.OrderComparator;
import org.springframework.security.core.parameters.P;
import org.zeroagent.common.utils.Asserts;

import java.util.*;

/**
 * 管理AI任务的事务 - 同步回调
 * @author Nuk3m1
 * @version 2026年05月02日  23时06分
 */
@UtilityClass
public class AiTaskTransactionManager {

    /**
     * 本地线程内的Ai任务同步函数列表
     */
    private static final ThreadLocal<Set<AiTaskSynchronization>> synchronizations =
            new NamedThreadLocal<>("AiTask synchronizations");


    public static boolean isSynchronizationActive() {
        return synchronizations.get() != null;
    }

    public static void initSynchronization() throws IllegalStateException {
        if (isSynchronizationActive()) {
            throw new IllegalStateException("AiTaskSynchronization is already active");
        }
        synchronizations.set(new LinkedHashSet<>());
    }

    public static boolean isSynchronizationEmpty() {
        Set<AiTaskSynchronization> set = synchronizations.get();
        return set == null || set.isEmpty();
    }

    /**
     * 将同步事务注册到当前线程内
     * @param synchronization 同步函数
     * @throws IllegalStateException if synchronization is not active
     */
    public void registerSynchronization(AiTaskSynchronization synchronization) throws IllegalStateException {
        Asserts.notNull(synchronization, "synchronization must be not null");
        Set<AiTaskSynchronization> set = synchronizations.get();
        if (set == null) {
            throw new IllegalStateException("AiTaskSynchronization is not active");
        }
        set.add(synchronization);
    }

    /**
     * 获取当前线程内的同步函数列表
     * @return 同步函数列表 (只读)
     * @throws IllegalStateException if synchronization is not active
     */
    public static List<AiTaskSynchronization> getSynchronizations() throws IllegalStateException {
        Set<AiTaskSynchronization> synchs = synchronizations.get();
        if (synchs == null) {
            throw new IllegalStateException("AiTaskSynchronization is not active");
        }
        // 当未发生同步函数提交时，返回空列表，下游避免创建事务，节省性能
        if (synchs.isEmpty()) {
            return Collections.emptyList();
        }
        // 打包优先级，封装为只读(Read-Only View)列表
        List<AiTaskSynchronization> sortedSynchs = new ArrayList<>(synchs);
        OrderComparator.sort(sortedSynchs);
        return Collections.unmodifiableList(sortedSynchs);
    }

    /**
     * 清理 ThreadLocal 防止泄漏 (弱饮用ThreadLocal)
     * @throws IllegalStateException if AiTaskSynchronization si not active
     */
    public static void clearSynchronizations() throws IllegalStateException {
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("Cannot deactivate AiTaskSynchronization - not active");
        }
        synchronizations.get().clear();
        synchronizations.remove();
    }


}
