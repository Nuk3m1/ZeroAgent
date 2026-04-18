package org.zeroagent.infra.integration.tos.config;

import com.volcengine.tos.TOSClientConfiguration;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  13时54分
 */
@Validated
@Data
@ConfigurationProperties("volcengine.cloud")
public class VolCloudProperties {
    @NotBlank
    private String region;
    @NotBlank
    private String accessKey;
    @NotBlank
    private String secretKey;


    /**
     * 是否开启CRC校验
     */
    private boolean enableCrc = true;
    /**
     * 建立连接的超时时间  单位：ms
     */
    private int     connectTimeoutMills     = 5 * 60 * 1000;
    /**
     * HTTP读请求超时时间  单位：ms
     */
    private int     readTimeoutMills         = 5 * 60 * 1000;
    /**
     * HTTP写请求超时时间  单位：ms
     */
    private int     writeTimeoutMills        = 5 * 60 * 1000;
    /**
     * 失败重试次数
     */
    private int     maxRetryCount         = 3;
}
