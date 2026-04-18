package org.zeroagent.infra.integration.feishu.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月17日  17时11分
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FeiShuProperties.class)
public class FeiShuHttpConfiguration {
    @Bean
    public WebClient feishuSendClient(FeiShuProperties feiShuProperties) {
        return WebClient.builder()
                .baseUrl(feiShuProperties.getFeishuUrl())
                .build();
    }
}
