package org.zeroagent.infra.integration.tos.config;

import com.volcengine.tos.TOSClientConfiguration;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.credential.StaticCredentialsProvider;
import com.volcengine.tos.transport.TransportConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  13时54分
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({VolTosProperties.class, VolCloudProperties.class})
public class TosContextConfiguration {

    @Bean
    public TOSV2 tosClient(VolTosProperties volTosProperties, VolCloudProperties volCloudProperties) {
        return new TOSV2ClientBuilder().build(tosClientConfiguration(volCloudProperties, volTosProperties));
    }

    private TOSClientConfiguration tosClientConfiguration(VolCloudProperties volCloudProperties, VolTosProperties volTosProperties) {
        TransportConfig transportConfig = TransportConfig.builder()
                .connectTimeoutMills(volCloudProperties.getConnectTimeoutMills())
                .readTimeoutMills(volCloudProperties.getReadTimeoutMills())
                .writeTimeoutMills(volCloudProperties.getWriteTimeoutMills())
                .maxRetryCount(volCloudProperties.getMaxRetryCount())
                .build();
        TOSClientConfiguration tosClientConfiguration = TOSClientConfiguration.builder()
                .region(volCloudProperties.getRegion())
                .endpoint(volTosProperties.getEndpoint())
                .credentialsProvider(new StaticCredentialsProvider(volCloudProperties.getAccessKey(), volCloudProperties.getSecretKey()))
                .transportConfig(transportConfig)
                .enableCrc(volCloudProperties.isEnableCrc())
                .build();
        return tosClientConfiguration;
    }

}
