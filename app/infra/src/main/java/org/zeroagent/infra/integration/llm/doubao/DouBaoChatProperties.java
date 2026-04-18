package org.zeroagent.infra.integration.llm.doubao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chenhua
 * @version 2026年03月05日  14时53分
 * @Description:
 */
@Data
@ConfigurationProperties("zeroagent.llm.doubao.chat")
public class DouBaoChatProperties {
    private String            completionPath = "/v3/chat/completions";
    private String            model ;

}
