package com.tinsiag.tinsiagaicodemother.langgraph4j.ai;

import com.tinsiag.tinsiagaicodemother.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ImageCollectionServiceTest {

    @Resource
    private ImageCollectionService imageCollectionService;

    @Test
    void testTechWebsiteImageCollection() {
        List<ImageResource> imageResourceList = imageCollectionService.collectImages("创建一个技术博客网站，需要展示编程教程和系统架构");
        Assertions.assertNotNull(imageResourceList);
        System.out.println("技术网站收集到的图片: " + imageResourceList);
    }

    @Test
    void testEcommerceWebsiteImageCollection() {
        List<ImageResource> imageResourceList = imageCollectionService.collectImages("创建一个电商购物网站，需要展示商品和品牌形象");
        Assertions.assertNotNull(imageResourceList);
        System.out.println("电商网站收集到的图片: " + imageResourceList);
    }
}
