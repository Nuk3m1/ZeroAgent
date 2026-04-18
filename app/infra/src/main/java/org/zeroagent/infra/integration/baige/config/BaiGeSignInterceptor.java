package org.zeroagent.infra.integration.baige.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  20时46分
 */
@RequiredArgsConstructor
public class BaiGeSignInterceptor implements RequestInterceptor {
    private final BaiGeApiProperties baiGeApiProperties;

    @Override
    public void apply(RequestTemplate template) {
        template.header("Content-Type", "application/json");
    }

}
