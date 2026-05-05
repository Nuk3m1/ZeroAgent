package org.zeroagent.domain.core.ai.chat.service;

import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月23日  13时59分
 */
public interface ToolCallingTemplate {
    Flux<MessageChunk> toolCallingChat(LlmRequest llmRequest, Map<Integer, ToolCallingIntent> intentMap);

}
