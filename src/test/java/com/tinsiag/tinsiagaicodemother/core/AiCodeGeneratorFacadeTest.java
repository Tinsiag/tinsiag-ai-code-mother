package com.tinsiag.tinsiagaicodemother.core;

import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;


@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void generateCodeAndSave() {
        File file = aiCodeGeneratorFacade.generateCodeAndSave("做一个简单小新iften的博客，不超过50行", CodeGenTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateCodeAndSaveStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateCodeAndSaveStream("做一个简单小新的博客，不超过50行", CodeGenTypeEnum.HTML);
        List<String> block = stringFlux.collectList().block();
        Assertions.assertNotNull(block);
        String completeContent = String.join("", block);
        Assertions.assertNotNull(completeContent);
    }
}