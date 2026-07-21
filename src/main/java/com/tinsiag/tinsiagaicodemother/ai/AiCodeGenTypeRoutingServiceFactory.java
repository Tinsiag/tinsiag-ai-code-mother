package com.tinsiag.tinsiagaicodemother.ai;

import com.tinsiag.tinsiagaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {

    @Bean
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        ChatModel routingChatModelConfigPrototype = SpringContextUtil.getBean("RoutingChatModelConfigPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                        .chatModel(routingChatModelConfigPrototype)
                        .build();
    }

    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return createAiCodeGenTypeRoutingService();
    }
}
