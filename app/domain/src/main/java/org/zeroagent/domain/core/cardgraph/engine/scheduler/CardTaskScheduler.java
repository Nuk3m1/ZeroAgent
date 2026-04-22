package org.zeroagent.domain.core.cardgraph.engine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.zeroagent.domain.common.async.AsyncPools;
import org.zeroagent.domain.common.async.AsyncTemplate;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.cardgraph.engine.CardTaskEngine;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 卡牌任务定时调度器
 * @author Nuk3m1
 * @version 2026年04月12日  23时01分
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.card-task.scheduler", value = "enabled", havingValue = "true")
public class CardTaskScheduler {
    private final CardInformationRepository cardInformationRepository;
    private final AsyncTemplate             asyncTemplate;
    private final TransactionTemplate       transactionTemplate;
    private final AppAlertHelper            appAlertHelper;
    private final CardTaskEngine            cardTaskEngine;

    private final static Duration TIMEOUT = Duration.ofSeconds(30);
    // 每次捞取数量
    private final static int LOAD_LIMIT = 10;
    /**
     * 此处只设置触发器，具体的建模逻辑由引擎层解决
     */
    @Scheduled(initialDelay = 10, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void pollCardNodeBuild() {
        // 通过事务 + 行锁 - 避免多机器重复捞取相同卡牌信息
        List<CardInformation> cardTasks = transactionTemplate.execute(status -> {
            List<CardInformation> fetchTasks = cardInformationRepository.fetchBatchByStatus(LOAD_LIMIT, CardInformationStatusEnum.PENDING);
            fetchTasks.forEach(cardInformation -> {
                cardInformationRepository.updateExecuteStatusById(cardInformation.getId(), CardInformationStatusEnum.EXECUTING);
            });
            return fetchTasks;
        });
        // 提交异步线程池
        if (cardTasks != null && !cardTasks.isEmpty()) {
            asyncTemplate.execute(AsyncPools.CARD_SCHEDULER_EXECUTE_POOL, () -> cardTaskEngine.execute(cardTasks));
        }

    }
}
