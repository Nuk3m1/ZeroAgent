package org.zeroagent.domain.core.cardgraph.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.cardgraph.service.CardGraphRepository;

import java.util.List;

/**
 * 卡牌任务引擎实现
 * @author Nuk3m1
 * @version 2026年04月12日  23时03分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardTaskEngineImpl implements CardTaskEngine {
    private final CardGraphRepository       cardGraphRepository;
    private final CardInformationRepository cardInformationRepository;


    @Override
    public void execute(@NotNull List<CardInformation> cardInformationList) {
        for (CardInformation cardInformation : cardInformationList) {
            try {
                String passcode = cardInformation.getPasscode();
                cardGraphRepository.createCardNodeEntity(
                        passcode,
                        cardInformation.getName(),
                        cardInformation.getEffect(),
                        cardInformation.getCardType().name()
                );
                if (StringUtils.hasText(cardInformation.getRace())) {
                    cardGraphRepository.drawRaceArrow(passcode, cardInformation.getRace());
                }
                if (StringUtils.hasText(cardInformation.getAttribution())) {
                    cardGraphRepository.drawAttributeArrow(passcode, cardInformation.getAttribution());
                }
                List<String> archetypeList = cardInformation.getArchetype();
                if (archetypeList != null && !archetypeList.isEmpty()) {
                    archetypeList.forEach(archetype -> {
                        cardGraphRepository.drawArchetypeArrow(passcode, archetype);
                    });
                }

                cardInformationRepository.updateExecuteStatusById(cardInformation.getId(), CardInformationStatusEnum.SUCCESS);
            } catch (Exception e) {
                cardInformationRepository.updateExecuteStatusById(cardInformation.getId(), CardInformationStatusEnum.FAILURE);
                log.error("卡密为 {} 的卡牌 {} 生成图数据库节点异常！", cardInformation.getPasscode(), cardInformation.getName(), e);
            }
        }
    }
}
