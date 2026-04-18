package org.zeroagent.infra.integration.baige.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  20时41分
 */
@Data
@ConfigurationProperties(prefix =  "zeroagent.baige")
public class BaiGeApiProperties {
    private String url ;
}
