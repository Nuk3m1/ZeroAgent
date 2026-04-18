package org.zeroagent.infra.integration;

import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  20时34分
 */
@Configuration
@RequiredArgsConstructor
public class IntegrationConfiguration {

    @Bean
    public OkHttpClient okHttpClient() {
        okhttp3.OkHttpClient delegate = new okhttp3.OkHttpClient.Builder()
                // 建立连接超时
                .connectTimeout(Duration.ofSeconds(10))
                // 请求服务器超时
                .writeTimeout(Duration.ofSeconds(10))
                // 等待服务器响应超时
                .readTimeout(Duration.ofSeconds(10))
                // 总体调用耗时 (包含 DNS解析 + 请求 + 响应)
                .callTimeout(Duration.ofSeconds(15))
                .build();
        return new OkHttpClient(delegate);
    }
}
