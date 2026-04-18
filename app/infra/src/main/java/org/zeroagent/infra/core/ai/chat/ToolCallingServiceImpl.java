package org.zeroagent.infra.core.ai.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.core.ai.chat.model.message.Message;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingExecutor;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingFactory;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月15日  14时19分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ToolCallingServiceImpl implements ToolCallingService {
    private final ToolCallingFactory toolCallingFactory;

    @Override
    public List<ToolCallingBizResult> executeToolCalling(List<ToolCallingIntent> intents) {
        List<ToolCallingBizResult> result = new ArrayList<>();
        for (ToolCallingIntent intent : intents) {
            ToolCallingExecutor executor = toolCallingFactory.getExecutor(intent.getToolName());
            if (executor != null) {
                // 1. 执行本地工具
                String dbResult = executor.execute(intent.getToolArgumentsJson());

                // 2. 包装成结果
                ToolCallingBizResult toolCallingBizResult = new ToolCallingBizResult();
                toolCallingBizResult.setToolCallId(intent.getToolCallId());
                toolCallingBizResult.setToolName(intent.getToolName());
                toolCallingBizResult.setToolArgumentsJson(intent.getToolArgumentsJson());
                toolCallingBizResult.setDbResult(dbResult);
                result.add(toolCallingBizResult);
            }
        }
        return result;
    }
}
