package org.zeroagent.api.core.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zeroagent.biz.chat.AiChatManager;
import org.zeroagent.common.result.ApiResult;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import org.zeroagent.domain.core.ai.chat.model.request.AiChatRequestVO;
import reactor.core.publisher.Flux;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  16时58分
 * @Description:
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/user/ai-chat", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AiChatResource {
    private final AiChatManager aiChatManager;



    @PostMapping(path = "/conversations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private Flux<ApiResult<MessageChunk>> chat(@RequestBody AiChatRequestVO request) {
        return aiChatManager.chat(request)
                .map(ApiResult::success);
    }

    @PostMapping(path = "/extract")
    private void test() {
        aiChatManager.extractRulesTest();
    }
}
