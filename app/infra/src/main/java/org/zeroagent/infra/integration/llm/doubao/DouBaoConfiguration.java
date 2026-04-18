package org.zeroagent.infra.integration.llm.doubao;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author chenhua
 * @version 2026年03月05日  14时55分
 * @Description:
 */
@Configuration
@EnableConfigurationProperties({DouBaoChatProperties.class, DouBaoConnectionProperties.class})
public class DouBaoConfiguration {
    @Bean
    public WebClient doubaoChatClient(DouBaoChatProperties douBaoChatProperties,
                                      DouBaoConnectionProperties douBaoConnectionProperties) {
        return WebClient.builder()
                .baseUrl(douBaoConnectionProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + douBaoConnectionProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

    }

    @Bean
    public RestClient doubaoToolCallingClient(DouBaoChatProperties douBaoChatProperties,
                                              DouBaoConnectionProperties douBaoConnectionProperties) {
    return RestClient.builder()
            .baseUrl(douBaoConnectionProperties.getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + douBaoConnectionProperties.getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }


}
