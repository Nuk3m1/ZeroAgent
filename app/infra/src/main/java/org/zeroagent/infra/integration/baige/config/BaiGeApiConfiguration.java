package org.zeroagent.infra.integration.baige.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zeroagent.infra.integration.baige.api.BaiGeApi;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  20时42分
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BaiGeApiProperties.class)
public class BaiGeApiConfiguration {
    private final BaiGeApiProperties        baiGeApiProperties;
    private final ObjectMapper              objectMapper;
    private final OkHttpClient              okHttpClient;
    @Bean
    public BaiGeApi baiGeApi() {
        BaiGeSignInterceptor signInterceptor = new BaiGeSignInterceptor(baiGeApiProperties);
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client(okHttpClient)
                .retryer(Retryer.NEVER_RETRY)
                .requestInterceptor(signInterceptor)
                .target(BaiGeApi.class, baiGeApiProperties.getUrl());
    }

}
