package com.tinsiag.tinsiagaicodemother.core.handle;

import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.tinsiag.tinsiagaicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class SimpleTextStreamHandle {
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId,User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    String aiResponse = aiResponseBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponse)) {
                        chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    }
                })
                .doOnError(error -> {
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }
}
