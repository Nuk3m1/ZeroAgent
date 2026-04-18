package org.zeroagent.domain.common.integration.feishu;

import reactor.core.publisher.Mono;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月16日  22时37分
 */
public interface FeiShuClient {
    String sendInitialMessage(String chatId, String content);

    Mono<Void> updateFeiShuMessage(String messageId, String content);
}
