package com.tinsiag.tinsiagaicodemother.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.langgraph4j.model.ImageResource;
import com.tinsiag.tinsiagaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import com.tinsiag.tinsiagaicodemother.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.http.jdk.JdkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.image-model")
public class LogoGeneratorTool {
    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Duration timeout;

    @Resource
    private CosManager cosManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 构建 Logo 设计提示词
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);

            ImageModel model = OpenAiImageModel.builder()
                    .httpClientBuilder(SpringRestClient.builder())
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .timeout(timeout)
                    .size("512x512")
                    .build();

            Response<Image> response = model.generate(logoPrompt);
            Image image = response.content();
            byte[] bytes = Base64.getDecoder().decode(image.base64Data());
            String fileName = String.format("%s/%s.png",
                    RandomUtil.randomString(5), RandomUtil.randomString(10));
            File file = FileUtil.writeBytes(bytes, fileName);
            String upLoadFile = cosManager.upLoadFile(file, "AiCodeMother/logo/" + fileName);
            logoList.add(ImageResource.builder()
                    .category(ImageCategoryEnum.LOGO)
                    .description(description)
                    .url(upLoadFile)
                    .build());

            FileUtil.del(file);
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }
}
