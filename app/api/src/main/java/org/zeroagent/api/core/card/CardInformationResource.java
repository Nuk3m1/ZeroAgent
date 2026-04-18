package org.zeroagent.api.core.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zeroagent.biz.card.CardInformationManager;
import org.zeroagent.common.result.ApiResult;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月20日  18时11分
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/card", produces =  MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CardInformationResource {
    private final CardInformationManager cardInformationManager;


    @PostMapping("/fetch-all")
    public ApiResult<Boolean> fetchAll() {
        return ApiResult.success(cardInformationManager.fetchAllCards());
    }
}
