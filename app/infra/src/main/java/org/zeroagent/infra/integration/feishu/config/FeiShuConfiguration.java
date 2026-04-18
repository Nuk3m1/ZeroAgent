package org.zeroagent.infra.integration.feishu.config;

import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.ws.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.zeroagent.infra.integration.feishu.FeiShuEventListener;

/**
 * 飞书配置累 - 注册客户端Bean单例
 * @author Nuk3m1
 * @version 2026年04月16日  22时38分
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties(FeiShuProperties.class)
public class FeiShuConfiguration {
    private final FeiShuEventListener feiShuEventListener;

    @Bean(initMethod = "start")
    public Client feiShuEventReceiveClient(FeiShuProperties feiShuProperties) {
        EventDispatcher eventDispatcher = EventDispatcher.newBuilder("","")
                .onP2MessageReceiveV1(feiShuEventListener)
                .build();
        return new Client.Builder(feiShuProperties.getAppId(), feiShuProperties.getAppSecret())
                .eventHandler(eventDispatcher).build();
    }

}
