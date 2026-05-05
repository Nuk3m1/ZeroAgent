package org.zeroagent.domain.core.grapherror.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.cardgraph.model.GraphRelationTypeEnum;
import org.zeroagent.domain.core.grapherror.service.ExtractRelationshipService;

import java.util.List;

/**
 * 语义关系抽取任务引擎实现
 * @author Nuk3m1
 * @version 2026年04月23日  15时10分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticExtractTaskEngineImpl implements SemanticExtractTaskEngine {
    private final ExtractRelationshipService extractRelationshipService;
    private final CardInformationRepository  cardInformationRepository;

    @Override
    public void execute(@NotNull List<CardInformation> cardInformationList) {
        for (CardInformation cardInformation : cardInformationList) {
            try {
                extractRelationshipService.extractCardRules(
                                cardInformation.getPasscode(),
                                cardInformation.getName(),
                                cardInformation.getEffect(),
                                ToolCallingEnum.EXTRACT_SEARCH_INFORMATION_FROM_CARD,
                                GraphRelationTypeEnum.SEARCH
                        )
                        .block();
                cardInformationRepository.updateExecuteStatusById(cardInformation.getId(), CardInformationStatusEnum.COMPLETED);
            } catch (Exception e) {
                cardInformationRepository.updateExecuteStatusById(cardInformation.getId(), CardInformationStatusEnum.FAILURE);
                log.error("卡密为 {} 的卡牌 {} 执行语义关系抽取失败", cardInformation.getPasscode(), cardInformation.getName(), e);
            }
        }
    }
}
