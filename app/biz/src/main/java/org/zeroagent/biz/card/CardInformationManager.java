package org.zeroagent.biz.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zeroagent.domain.core.card.service.CardInformationFetchClient;
import org.zeroagent.domain.core.card.service.CardInformationRepository;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月20日  18时08分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardInformationManager {
    private final CardInformationRepository cardInformationRepository;
    private final CardInformationFetchClient cardInformationFetchClient;


    public boolean fetchAllCards() {
        try {
            cardInformationFetchClient.getCards();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
