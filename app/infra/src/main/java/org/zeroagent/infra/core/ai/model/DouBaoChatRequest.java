package org.zeroagent.infra.core.ai.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.experimental.Accessors;
import org.zeroagent.domain.core.ai.chat.model.message.Message;
import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  19时14分
 * @Description:
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DouBaoChatRequest implements LlmRequest {
    private String model;

    private List<Message> messages;
    private boolean stream = true;

    private List<ObjectNode> tools;

}
