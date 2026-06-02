package com.tinsiag.tinsiagaicodemother.core.handle;

import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import com.tinsiag.tinsiagaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class StreamHandlerExecutor {
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;


    public Flux<String> streamHandlerExecutor(Flux<String> originFlux, CodeGenTypeEnum codeGenTypeEnum, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        return switch (codeGenTypeEnum) {
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE ->
                    new SimpleTextStreamHandle().handle(originFlux, chatHistoryService, appId, loginUser);

        };
    }

}
