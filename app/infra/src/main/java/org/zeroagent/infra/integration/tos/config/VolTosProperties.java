package org.zeroagent.infra.integration.tos.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  13时52分
 */
@Validated
@Data
@ConfigurationProperties(prefix = "volcengine.cloud.tos")
public class VolTosProperties {
    @NotBlank
    private String endpoint;
    @NotBlank
    private String readEndpoint;
    @NotBlank
    private String bucket;
}
