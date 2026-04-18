package org.zeroagent.domain.core.ai.chat.toolcalling;

import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;

import java.util.List;

/**
 * 封装工具调用的查询上下文 - RestClient
 * @author Nuk3m1
 * @version 2026年04月14日  22时35分
 */
public interface ToolCallingService {

    List<ToolCallingBizResult> executeToolCalling(List<ToolCallingIntent> intents);
}
