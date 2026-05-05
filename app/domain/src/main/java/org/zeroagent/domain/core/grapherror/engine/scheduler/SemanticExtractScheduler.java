package org.zeroagent.domain.core.grapherror.engine.scheduler;

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
import org.zeroagent.domain.core.grapherror.engine.SemanticExtractTaskEngine;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 语义关系抽取定时调度器
 * @author Nuk3m1
 * @version 2026年04月23日  15时11分
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.semantic-extract.scheduler", value = "enabled", havingValue = "true")
public class SemanticExtractScheduler {
    /**
     * 每次捞取数量
     */
    private static final int LOAD_LIMIT = 1;

    private final CardInformationRepository cardInformationRepository;
    private final AsyncTemplate             asyncTemplate;
    private final TransactionTemplate       transactionTemplate;
    private final SemanticExtractTaskEngine semanticExtractTaskEngine;

    private final AtomicInteger counter = new AtomicInteger(0);
    private static final int MAX_RUNS = 1; // 假设只想跑5次

    /**
     * 捞取已完成结构化关系构建的卡牌，提交语义关系抽取任务
     */
    @Scheduled(initialDelay = 15, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void pollSemanticExtractTask() {
        if (counter.get() >= MAX_RUNS) {
            System.out.println("任务已达到最大执行次数，停止逻辑");
            return;
        }

        int current = counter.incrementAndGet();
        List<CardInformation> cardTasks = transactionTemplate.execute(status -> {
            List<CardInformation> fetchTasks = cardInformationRepository.fetchBatchByStatus(LOAD_LIMIT, CardInformationStatusEnum.SUCCESS);
            fetchTasks.forEach(cardInformation -> {
                cardInformationRepository.updateExecuteStatusById(cardInformation.getId(), CardInformationStatusEnum.EXECUTING);
            });
            return fetchTasks;
        });
        if (cardTasks != null && !cardTasks.isEmpty()) {
            asyncTemplate.execute(AsyncPools.SEMANTIC_EXTRACT_EXECUTE_POOL, () -> semanticExtractTaskEngine.execute(cardTasks));
        }
    }
}
