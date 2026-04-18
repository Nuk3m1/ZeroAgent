package org.zeroagent.infra.integration.feishu.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月16日  22时46分
 */
@Validated
@Data
@ConfigurationProperties(prefix = "zeroagent.feishu")
public class FeiShuProperties {
    @NotBlank
    private String feishuUrl;
    @NotBlank
    private String larksuiteUrl;
    @NotBlank
    private String appId;
    @NotBlank
    private String appSecret;
}
