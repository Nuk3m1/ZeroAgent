package org.zeroagent.infra.integration.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.zeroagent.domain.core.ai.chat.model.Conversation;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;
import org.zeroagent.domain.core.ai.chat.model.message.UserMessage;
import org.zeroagent.domain.core.ai.chat.service.AiChatService;
import org.zeroagent.infra.utils.DebugUtil;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 飞书事件处理器
 * @author Nuk3m1
 * @version 2026年04月17日  13时30分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FeiShuEventListener extends ImService.P2MessageReceiveV1Handler {
    private final AiChatService     aiChatService;
    private final FeiShuTemplate    feiShuTemplate;
    private final ObjectMapper      objectMapper;

    // 缓冲 Agent服务 与 FeiShu连接，防止请求超出上限
    @Data
    private static class ChatState {
        private StringBuilder reasoning = new StringBuilder();
        private StringBuilder content = new StringBuilder();

        private final AtomicInteger sequence = new AtomicInteger(0);
        public int incrementAndGetSequence() {
            return sequence.incrementAndGet();
        }
    }

    @Override
    public void handle(P2MessageReceiveV1 event) {
        log.info("Receive feishu event: {}", event);
        String rawContent = event.getEvent().getMessage().getContent();
        String chatId = event.getEvent().getMessage().getChatId();
        String userInput = getRealText(rawContent);
        if (!StringUtils.hasText(userInput)) {
            log.warn("解析出用户输出为空: {}", rawContent);
            return;
        }
        Mono.fromCallable(() -> {
            String cardId = feiShuTemplate.createCardEntity();
            return feiShuTemplate.sendMessage(chatId, cardId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(cardId -> processReactiveChat(userInput, cardId))
                .subscribe(
                        null,
                        err -> {
                            Throwable cause = Exceptions.unwrap(err);
                            if (cause instanceof WebClientResponseException ex) {
                                log.error("飞书 API 拒绝请求: HTTP {}, 详细原因: {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
                            } else {
                                log.error("飞书响应通道崩溃", cause);
                            }
                        }
                );

    }
    private Flux<Void> processReactiveChat(String userInput, String messageId) {
        // 最新状态 时刻追加写
        AtomicReference<ChatState> lastStateRef = new AtomicReference<>(new ChatState());
        Conversation conversation = new  Conversation()
                .setTitle("FeiShuChat:" + messageId);
        UserMessage userMessage = new UserMessage(MediaType.TEXT, userInput);
        return aiChatService.DouBaoChatStream(conversation, userMessage, userInput)
                .scanWith(ChatState::new, (state, chunk) -> {
                    if (StringUtils.hasLength(chunk.getReasoningContent())) {
                        state.getReasoning().append(chunk.getReasoningContent());
                    }
                    if (StringUtils.hasLength(chunk.getContent())) {
                        state.getContent().append(chunk.getContent());
                    }
                    return state;
                })
                .skip(1)
                .doOnNext(lastStateRef::set)
                .bufferTimeout(10, Duration.ofMillis(200))
                .sample(Duration.ofMillis(200))
                .onBackpressureLatest()
                .map(list -> list.get(list.size() - 1))
                .concatMap(state -> {
                    boolean hasReasoning = StringUtils.hasText(state.getReasoning().toString());
                    boolean hasContent = StringUtils.hasText(state.getContent().toString());
                    Mono<Void> updateTask = Mono.empty();
                    if (hasReasoning) {
                        updateTask = updateTask.then(Mono.defer(() ->
                            feiShuTemplate.streamUpdateElement(
                                    messageId,
                                    "reasoning_content",
                                    state.getReasoning().toString(),
                                    state.incrementAndGetSequence()
                            )
                        ));
                    }
                    if (hasContent) {
                        updateTask = updateTask.then(Mono.defer(() ->
                                feiShuTemplate.streamUpdateElement(
                                        messageId,
                                        "main_content",
                                        state.getContent().toString(),
                                        state.incrementAndGetSequence()
                                )
                        ));
                    }
                    return updateTask
                            .timeout(Duration.ofSeconds(3))
                            .retryWhen(Retry.backoff(1, Duration.ofMillis(500)));

                })
                // 最终态处理
                .concatWith(Mono.defer(() -> {
                    ChatState state = lastStateRef.get();
                    int seq = state.incrementAndGetSequence();
                    Mono<Void> finishReasoning = StringUtils.hasText(state.getReasoning().toString()) ?
                            feiShuTemplate.streamUpdateElement(messageId, "reasoning_content", state.getReasoning().toString(), seq) :
                            Mono.empty();
                    Mono<Void> finishContent = StringUtils.hasText(state.getContent().toString()) ?
                            feiShuTemplate.streamUpdateElement(messageId, "main_content", state.getContent().toString(), seq) :
                            Mono.empty();
                    return Mono.when(finishReasoning, finishContent);
                }))
                .onErrorResume(err -> {
                    log.error("飞书响应流中断", err);
                    ChatState errState = lastStateRef.get();
                    return feiShuTemplate.streamUpdateElement(messageId, "main_content", errState.getContent().toString() + "\n\n [网络中断]", errState.incrementAndGetSequence());
                });

    }



    private String getRealText(String rawContent) {
        try {
            JsonNode node = objectMapper.readTree(rawContent);
            if (node.has("text")) {
                return node.get("text").asText();
            }
        } catch (Exception e) {
            log.warn("飞书消息解析失败，直接使用原始消息", e);

        }
        return rawContent;
    }
}
