package org.zeroagent.infra.core.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.domain.common.reactive.ErrorMapper;
import org.zeroagent.domain.core.ai.chat.error.ChatErrorCode;
import org.zeroagent.domain.core.ai.chat.model.Conversation;
import org.zeroagent.domain.core.ai.chat.model.ConversationContext;
import org.zeroagent.domain.core.ai.chat.model.ConversationMessage;
import org.zeroagent.domain.core.ai.chat.model.SystemPromptPool;
import org.zeroagent.domain.core.ai.chat.model.media.MediaType;
import org.zeroagent.domain.core.ai.chat.model.message.*;
import org.zeroagent.domain.core.ai.chat.model.request.LlmRequest;
import org.zeroagent.domain.core.ai.chat.model.response.LlmResponse;
import org.zeroagent.domain.core.ai.chat.model.response.MessageChunk;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingBizResult;
import org.zeroagent.domain.core.ai.chat.model.toolcalling.ToolCallingIntent;
import org.zeroagent.domain.core.ai.chat.service.AiChatService;
import org.zeroagent.domain.core.ai.chat.service.ConversationMessageRepository;
import org.zeroagent.domain.core.ai.chat.service.ConversationRepository;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingEnum;
import org.zeroagent.domain.core.ai.chat.toolcalling.ToolCallingFactory;
import org.zeroagent.domain.core.utils.TextUtil;
import org.zeroagent.infra.core.ai.toolcalling.ToolCallingExecutionTemplate;
import org.zeroagent.infra.integration.WebClientFactory;
import org.zeroagent.infra.integration.llm.doubao.DouBaoChatProperties;
import org.zeroagent.infra.core.ai.model.DouBaoChatRequest;
import org.zeroagent.infra.core.ai.model.DoubaoChatResponse;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Nuk3m1
 * @version 2026年03月05日  17时02分
 * @Description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {
    private final TransactionTemplate               transactionTemplate;
    private final ToolCallingExecutionTemplate      toolCallingExecutionTemplate;
    private final ConversationMessageRepository     conversationMessageRepository;
    private final ConversationRepository            conversationRepository;
    private final WebClientFactory                  webClientFactory;
    private final ObjectMapper                      objectMapper;
    private final DouBaoChatProperties              douBaoChatProperties;
    private final ToolCallingFactory                toolCallingFactory;

    @Override
    public Flux<MessageChunk> DouBaoChatStream(Conversation conversation, UserMessage userMessage, String userInput) {
        final boolean firstRound;
        final long conversationId;
        // TODO 用户上下文(UserContext)模块尚未完成
        // final long uid = conversation.getUid();

        List<Message> messages = new ArrayList<>();
        // 插入系统提示词 role : system
        messages.add(new SystemMessage(MediaType.TEXT, SystemPromptPool.CARD_INFORMATION_SYSTEM_PROMPT));
        // 历史记忆载入
        if (conversation.getId() == null) {
            // 初始化会话
            firstRound = true;
            conversationId = Long.parseLong(IdHelper.getStrId());
            conversation.setId(conversationId);
            // 持久化到数据库
            Conversation convModel = new Conversation().setId(conversationId);
            ConversationMessage conversationMessage = new ConversationMessage()
                    .setId(IdHelper.getId())
                    .setConversationId(conversationId)
                    .setRole(MessageType.USER)
                    .setContent(userMessage.getContent());
            transactionTemplate.execute(status -> {
               conversationRepository.create(convModel);
               conversationMessageRepository.create(conversationMessage);
               return convModel.getId();
            });
        } else {
            // 添加历史对话记录
            firstRound = false;
            conversationId = conversation.getId();
            // 持久化到数据库
            ConversationMessage conversationMessage = new ConversationMessage()
                    .setId(IdHelper.getId())
                    .setConversationId(conversationId)
                    .setRole(MessageType.USER)
                    .setContent(userMessage.getContent());

            // 请求体 载入历史记录
            messages.addAll(conversationMessageRepository.queryByConversationId(conversationId)
                    .stream()
                    .map(convMessage -> {
                        return switch (convMessage.getRole()) {
                            case USER -> new UserMessage(convMessage.getContent());
                            case SYSTEM -> new SystemMessage(convMessage.getContent());
                            case ASSISTANT ->  new AssistantMessage(convMessage.getContent());
                            case TOOL -> new ToolMessage(convMessage.getContent());
                        };
                    }).toList());
            conversationMessageRepository.create(conversationMessage);
        }
        // 插入用户提示词 role : user
        messages.add(userMessage);

        ConversationContext context = new ConversationContext()
                //.setUid()
                .setConversationId(conversation.getId())
                .setFirstRound(firstRound)
                .setUserInput(userMessage.getContent())
                .setMessageChunks(new ArrayList<>());

        DouBaoChatRequest requestBody = new DouBaoChatRequest()
                .setModel(douBaoChatProperties.getModel())
                .setMessages(messages);
        if (requestBody.getTools() == null || requestBody.getTools().isEmpty()) {
            // 通过枚举类载入 Agent 的工具箱
            List<ObjectNode> allTools = toolCallingFactory.getToolDefinitionNodes(ToolCallingEnum.GET_CARD_KNOWLEDGE_BY_NAME);
            requestBody.setTools(allTools);
        }
        return chatWithAgent(requestBody, context, userInput)
                .doOnNext(context.getMessageChunks()::add)
                .doOnComplete(() -> context.setSuccess(true))
                .doFinally(signalType -> this.updateConversationComplete(context, userInput));
    }

    // userInput不会多次重复传入，已经被隔离在DouBaoChatStream()方法中
    public Flux<MessageChunk> chatWithAgent(LlmRequest request, ConversationContext context, String userInput) {
        Map<Integer, ToolCallingIntent> intentMap = new HashMap<>();
        Flux<MessageChunk> LlmStream = webClientFactory.doubaoChatWebClient()
                .post()
                .uri(douBaoChatProperties.getCompletionPath())
                .bodyValue(objectMapper.valueToTree(request))
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorMap(ErrorMapper.sysException(ChatErrorCode.CHAT_CALL_LLM_ERROR))
                .doOnError(context::setError)
                .filter(StringUtils::hasText)
                .map(line -> line.replaceFirst("^data: ", "").trim())
                .map(this::toMessageChunk)
                .doOnNext(chunk -> {
                    if (chunk.getToolCallFragments() != null && !chunk.getToolCallFragments().isEmpty()) {
                        for (MessageChunk.ToolCallFragment toolCallFragment : chunk.getToolCallFragments()) {
                            Integer index = toolCallFragment.getIndex();
                            ToolCallingIntent intent = intentMap.computeIfAbsent(index, key -> new ToolCallingIntent());
                            if (StringUtils.hasText(toolCallFragment.getToolCallId())) {
                                intent.setToolCallId(toolCallFragment.getToolCallId());
                            }
                            if (StringUtils.hasText(toolCallFragment.getToolName())) {
                                intent.setToolName(toolCallFragment.getToolName());
                            }
                            if (StringUtils.hasText(toolCallFragment.getToolArgumentsFragment())) {
                                String currentArgs = intent.getToolArgumentsJson() == null ? "" : intent.getToolArgumentsJson();
                                intent.setToolArgumentsJson(currentArgs + toolCallFragment.getToolArgumentsFragment());
                            }
                        }
                    }
                });
        return LlmStream.concatWith(Flux.defer(()-> {
            // 发现需要工具调用
                if (!intentMap.isEmpty()) {
                    List<ToolCallingIntent> intents = new ArrayList<>(intentMap.values());
                    List<ToolCallingBizResult> results = toolCallingExecutionTemplate.executeToolCalling(intents);
                // 更新上下文 （工具调用结果）
                AssistantMessage assistantMessage = new AssistantMessage(MediaType.TEXT, "");
                assistantMessage.getMetadata().put("tool_calls", ToolCallingIntent.formatIntentsForLlm(intents));
                request.getMessages().add(assistantMessage);

                for (ToolCallingBizResult result : results) {
                    Map<String, Object> metadata = Map.of("tool_call_id", result.getToolCallId());
                    request.getMessages().add(new ToolMessage(MediaType.TEXT, result.getDbResult(), metadata));
                }
                return chatWithAgent(request, context, userInput);
            } else {
                return Flux.empty();
            }
        }));
    }

    /**
     *  更新消息和标题
     * @param context
     * @param userInput
     */
    private void updateConversationComplete(ConversationContext context, String userInput) {
        // 新增Assistant消息

        String reasoningContent = context.getMessageChunks()
                .stream()
                .map(MessageChunk::getReasoningContent)
                .dropWhile(org.apache.commons.lang3.StringUtils::isBlank)
                .collect(Collectors.joining());
        String replyText = context.getMessageChunks()
                .stream()
                .map(MessageChunk::getContent)
                .dropWhile(org.apache.commons.lang3.StringUtils::isBlank)
                .collect(Collectors.joining());
        AssistantMessage assistantMessage = new AssistantMessage(MediaType.TEXT, replyText);
        ConversationMessage conversationMessage = new ConversationMessage()
                .setId(IdHelper.getId())
                .setConversationId(context.getConversationId())
                .setRole(MessageType.ASSISTANT)
                .setContent(assistantMessage.getContent())
                .setReasoningContent(reasoningContent);
        conversationMessageRepository.create(conversationMessage);
        // 更新会话标题
        if (context.isFirstRound()) {
            String title = TextUtil.getTopWords(userInput, 12);
            Conversation updatingConversation = new Conversation().setId(context.getConversationId()).setTitle(title);
            conversationRepository.updateById(updatingConversation);
        }

    }

    /**
     *  String 反序列化为 MessageChunk
     * @param data String类型响应体
     * @return MessageChunk模型
     */
    private MessageChunk toMessageChunk(String data) {
        log.info(data);
        if ("[DONE]".equals(data)) {
            return MessageChunk.done();
        }
        try {
            LlmResponse response = objectMapper.readValue(data, DoubaoChatResponse.class);
            return MessageChunk.from(response);
        } catch (Exception e) {
            log.error("解析DouBaoChatAPI Response异常: {}", data, e);
            // 根据业务需求，这里可以选择抛出自定义异常，或者返回包含错误信息的 Chunk
            throw new RuntimeException("流解析失败", e);
        }
    }


}
