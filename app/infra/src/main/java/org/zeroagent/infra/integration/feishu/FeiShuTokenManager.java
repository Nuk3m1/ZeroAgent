package org.zeroagent.infra.integration.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.domain.common.integration.feishu.error.FeiShuErrorCode;
import org.zeroagent.infra.integration.WebClientFactory;
import org.zeroagent.infra.integration.feishu.config.FeiShuProperties;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一管理 飞书API Token
 * @author Nuk3m1
 * @version 2026年04月17日  16时27分
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(FeiShuProperties.class)
public class FeiShuTokenManager {
    private final @Qualifier("feishuSendClient") WebClient feishuSendWebClient;
    private final FeiShuProperties properties;
    // 内部缓存类 TODO 后续系统接入Redis 拓展 infra-Caching
    private record FeiShuTokenCache(String token, Instant expireAt) {}

    private final AtomicReference<FeiShuTokenCache> tokenCacheRef = new AtomicReference<>();

    public Mono<String> getTenantAccessToken() {
        FeiShuTokenCache tokenCache = tokenCacheRef.get();
        // 若缓存存在，且过期时间还有 5 分钟以上，直接使用本地缓存。
        if (tokenCache != null && tokenCache.expireAt.isAfter(Instant.now().plusSeconds(300))) {
            return Mono.just(tokenCache.token);
        }
        // 否则，请求刷新Token
        Map<String, String> body = Map.of(
                "app_id", properties.getAppId(),
                "app_secret", properties.getAppSecret()
        );
        return feishuSendWebClient.post()
                .uri("/auth/v3/tenant_access_token/internal")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> {
                    if (node.has("code") && node.get("code").asInt() == 0) {
                        String token = node.get("tenant_access_token").asText();
                        // 默认 7200 秒 过期
                        int expireAt = node.get("expire").asInt();
                        tokenCacheRef.set(new FeiShuTokenCache(token, Instant.now().plusSeconds(expireAt)));
                        return token;
                    } else {
                        throw new BizException(FeiShuErrorCode.GET_TOKEN_FAILED, node);
                    }
                });
    }
    public String getTenantAccessTokenSync() {
        return getTenantAccessToken().block();
    }
}
