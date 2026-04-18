package org.zeroagent.domain.core.ai.chat.model.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.zeroagent.domain.core.ai.chat.model.message.Message;

import java.util.List;
import java.util.Map;

/**
 * 大模型对话请求类 - 接口
 * @author Nuk3m1
 * @version 2026年04月15日  14时53分
 */
public interface LlmRequest {
    String getModel();
    LlmRequest setModel(String model);
    List<Message> getMessages();
    LlmRequest setMessages(List<Message> messages);
    List<ObjectNode> getTools();
    LlmRequest setTools(List<ObjectNode> tools);
}
