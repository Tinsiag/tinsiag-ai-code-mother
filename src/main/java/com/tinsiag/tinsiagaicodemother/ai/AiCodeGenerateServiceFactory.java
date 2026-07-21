package com.tinsiag.tinsiagaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tinsiag.tinsiagaicodemother.ai.tools.ToolsManager;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import com.tinsiag.tinsiagaicodemother.service.ChatHistoryService;
import com.tinsiag.tinsiagaicodemother.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGenerateServiceFactory {
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource(name = "openAiStreamingChatModel")
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;



    @Resource
    private ToolsManager toolsManager;

    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<String, AiCodegeneraorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("Removing AiCodegeneraorService for CacheKey: {}, cause: {}", key, cause);
            })
            .build();

    /**
     * 创建AiCodegeneraorService ,为了兼容老逻辑
     *
     * @return
     */
    public AiCodegeneraorService getAiCodeGenerateService(long appId) {
        return getAiCodeGenerateService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 创建AiCodegeneraorService
     *
     * @return
     */
    public AiCodegeneraorService getAiCodeGenerateService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return serviceCache.get(buildCacheKey(appId, codeGenTypeEnum), key -> createAiCodeGenerateService(appId, codeGenTypeEnum));
    }


    private AiCodegeneraorService createAiCodeGenerateService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        //从数据库中加载对话记录
        chatHistoryService.LoadChatMemoryToMemory(appId, chatMemory, 20);
        return switch (codeGenTypeEnum) {
            case HTML, MULTI_FILE -> {
                StreamingChatModel streamingChatModelPrototype = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodegeneraorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(streamingChatModelPrototype)
                        .chatMemory(chatMemory)
                        .build();
            }

            case VUE_PROJECT -> {
                StreamingChatModel reasoningStreamingChatModelPrototype = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodegeneraorService.class)
                        .streamingChatModel(reasoningStreamingChatModelPrototype)
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .tools(toolsManager.getAllTool()) // 调用工具
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest, "Error : there are no tools called " + toolExecutionRequest.name())) // 处理工具幻觉问题
                        .build();
            }
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum.getValue());
        };
    }

    @Bean
    public AiCodegeneraorService aiCodegeneraorService() {
        return getAiCodeGenerateService(0);
    }

    /**
     * 构造缓存Key
     *
     * @param appId
     * @param codeGenTypeEnum
     * @return
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return appId + "_" + codeGenTypeEnum.getValue();
    }
}
