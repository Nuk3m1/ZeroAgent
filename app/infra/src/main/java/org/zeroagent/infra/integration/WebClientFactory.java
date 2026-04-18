package org.zeroagent.infra.integration;

import com.lark.oapi.ws.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 统一管理 WebClient 客户端
 * @author Nuk3m1
 * @version 2026年03月05日  17时25分
 * @Description:
 */
@Component
public class WebClientFactory {
    private final @Qualifier("doubaoChatClient") WebClient doubaoChatClient;



    public WebClientFactory(@Qualifier("doubaoChatClient") WebClient doubaoChatClient
    ) {
        this.doubaoChatClient = doubaoChatClient;
    }

    public WebClient doubaoChatWebClient() {
        return doubaoChatClient;
    }


}
