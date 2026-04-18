package org.zeroagent.infra.integration.llm.doubao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenhua
 * @version 2026年03月05日  14时57分
 * @Description:
 */
@Data
@ConfigurationProperties("zeroagent.llm.doubao")
public class DouBaoConnectionProperties {
    public static final String DEFAULT_BASE_URL = "https://ark.cn-beijing.volces.com/api";

    private String apiKey;
    private String baseUrl = DEFAULT_BASE_URL;
}
