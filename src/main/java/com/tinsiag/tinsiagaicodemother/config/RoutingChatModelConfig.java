package com.tinsiag.tinsiagaicodemother.config;

import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.routing-chat-model")
@Data
public class RoutingChatModelConfig {
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer maxTokens;
    private Duration timeout;
    private Boolean logRequests;
    private Boolean logResponses;
    private Double temperature;

    @Bean
    @Scope("prototype")
    public ChatModel RoutingChatModelConfigPrototype() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(Boolean.TRUE.equals(logRequests))
                .logResponses(Boolean.TRUE.equals(logResponses))
                .timeout(timeout)
                .temperature(temperature)
                .httpClientBuilder(SpringRestClient.builder())
                .build();
    }
}
