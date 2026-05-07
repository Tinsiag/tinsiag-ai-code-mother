package com.tinsiag.tinsiagaicodemother.ai;

import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCodegeneraorServiceTest {

    @Resource
    private AiCodegeneraorService aiCodegeneraorService;
    @Test
    void generateHtmlCode() {
        HtmlCodeResult res = aiCodegeneraorService.generateHtmlCode("做一个简单的小新乁の个人博客，不超过50行");
        Assertions.assertNotNull(res);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult res = aiCodegeneraorService.generateMultiFileCode("做一个简单的小新乁の个人博客，不超过100行");
        Assertions.assertNotNull(res);
    }
}