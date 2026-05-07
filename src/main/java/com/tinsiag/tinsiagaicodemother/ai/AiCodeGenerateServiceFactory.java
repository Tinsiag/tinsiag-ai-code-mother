package com.tinsiag.tinsiagaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeGenerateServiceFactory {
    @Resource
    private ChatModel chatModel;

    /**
     * 创建AiCodegeneraorService
     * @return
     */
    @Bean
    public AiCodegeneraorService aiCodegeneraorService(){
        return AiServices.create(AiCodegeneraorService.class, chatModel);
    }
}
