package com.tinsiag.tinsiagaicodemother.manager;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest

class CosManagerTest {

    @Resource
    private CosManager cosManager;

    @Test
    void upLoadFile() {
        File file = new File("D:\\AProject\\tinsiag-ai-code-mother\\tmp\\web_screenshot\\ad626705\\74037_compressed.jpg");
        String url = cosManager.upLoadFile(file, "test1.jpg");
        assertNotNull(url);
    }
}