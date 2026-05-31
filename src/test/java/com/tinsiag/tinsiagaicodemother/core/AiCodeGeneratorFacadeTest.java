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
        File file = aiCodeGeneratorFacade.generateCodeAndSave("做一个简单小新iften的博客，不超过50行", CodeGenTypeEnum.HTML,1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateCodeAndSaveStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateCodeAndSaveStream("做一个简单小新的博客，不超过50行", CodeGenTypeEnum.HTML,2L);
        List<String> block = stringFlux.collectList().block();
        Assertions.assertNotNull(block);
        String completeContent = String.join("", block);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateCodeAndSaveStream(
                "简单的任务记录网站，总代码量不超过 500 行",
                CodeGenTypeEnum.VUE_PROJECT, 11111211L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}