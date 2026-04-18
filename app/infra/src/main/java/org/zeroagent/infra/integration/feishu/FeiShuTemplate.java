package org.zeroagent.infra.integration.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.domain.common.integration.feishu.error.FeiShuErrorCode;
import org.zeroagent.infra.integration.feishu.model.FeiShuCardEntityPool;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 飞书相关api操作类 - TODO 后续拓展QQ机器人后可抽象为一个的BotTemplate接口 然后实现
 * @author Nuk3m1
 * @version 2026年04月16日  22时38分
 */
@Component
@RequiredArgsConstructor
public class FeiShuTemplate {
    private final @Qualifier("feishuSendClient") WebClient          feishuSendWebClient;
    private final                                ObjectMapper       objectMapper;
    private final                                FeiShuTokenManager feiShuTokenManager;

    /**
     * 创建卡片实体
     * @return 卡片实体ID
     */
    public String createCardEntity() {
        String token = getTenantAccessTokenSync();
        // 构建 卡片JSON 2.0

        Map<String, Object> body = new HashMap<>();
        body.put("type", "card_json");

        try {
            JsonNode card = objectMapper.readTree(FeiShuCardEntityPool.OCG_CARD_ENTITY_JSON);
            body.put("data", objectMapper.writeValueAsString(card));
        } catch (Exception e) {
            throw new BizException(FeiShuErrorCode.CARD_SEND_ERROR, "卡片序列化失败", e);
        }
        JsonNode response = feishuSendWebClient.post()
                .uri("/cardkit/v1/cards")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        if (response != null && response.has("code") && response.get("code").asInt() == 0) {
            return response.get("data").get("card_id").asText();
        }
        throw new BizException(FeiShuErrorCode.CARD_SEND_ERROR,"发送初始化卡片失败 " + response);
    }
    public String sendMessage(String chatId, String cardId) {
        String token = getTenantAccessTokenSync();
        Map<String, Object> body = new HashMap<>();
        body.put("receive_id", chatId);
        body.put("msg_type", "interactive");
        Map<String, String> card = Map.of("card_id", cardId);
        Map<String, Object> content = Map.of(
                "type", "card",
                "data", card
        );
        try {
            body.put("content", objectMapper.writeValueAsString(content));
        } catch (Exception e) {
            throw new BizException(FeiShuErrorCode.CARD_SEND_ERROR,"卡片序列化失败", e);
        }
        JsonNode response = feishuSendWebClient.post()
                .uri("/im/v1/messages?receive_id_type=chat_id")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return cardId;
    }
    public Mono<Void> streamUpdateElement(String cardId, String elementId, String content, int sequence) {
        return feiShuTokenManager.getTenantAccessToken().flatMap(token -> {
            Map<String, Object> body = Map.of(
                    "content", content,
                    "sequence", sequence
            );
            return feishuSendWebClient.put()
                    .uri("/cardkit/v1/cards/" + cardId + "/elements/" + elementId + "/content")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .then();
        });
    }


    private String getTenantAccessTokenSync() {
        return feiShuTokenManager.getTenantAccessTokenSync();
    }
    private Mono<String> getTenantAccessTokenAsync() {
        return feiShuTokenManager.getTenantAccessToken();
    }
}
