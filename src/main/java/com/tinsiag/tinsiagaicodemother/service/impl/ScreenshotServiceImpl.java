package com.tinsiag.tinsiagaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.exception.ThrowUtils;
import com.tinsiag.tinsiagaicodemother.manager.CosManager;
import com.tinsiag.tinsiagaicodemother.service.ScreenshotService;
import com.tinsiag.tinsiagaicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {
    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), new IllegalArgumentException("webUrl不能为空"));
        // 本地截图
        String savedWebPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        // 上传图片到对象存储
        try {
            String objectStorageUrl = uploadScreenshotToObjectStorage(savedWebPageScreenshot);
            ThrowUtils.throwIf(StrUtil.isBlank(objectStorageUrl), new RuntimeException("上传截图到对象存储失败"));
            log.info("成功上传截图到对象存储，URL: {}", objectStorageUrl);
            return objectStorageUrl;
        } finally {
            cleanScreenshot(savedWebPageScreenshot);
        }

    }

    private String uploadScreenshotToObjectStorage(String localScreenshotPath){
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File file = new File(localScreenshotPath);
        if (!file.exists()) {
            log.error("本地截图文件不存在，路径: {}", localScreenshotPath);
            return null;
        }
        String key = "tinsiagaicodemother/"+ UUID.randomUUID().toString().substring(0,8)+".jpg";
        return cosManager.upLoadFile(file,key);
    }

    private void cleanScreenshot(String localScreenshotPath){
        if (StrUtil.isBlank(localScreenshotPath)) {
            return;
        }
        File file = new File(localScreenshotPath);
        if (file.exists()) {
            File parentFile = file.getParentFile();
            FileUtil.del(parentFile);
            log.info("已删除本地截图目录，路径: {}", parentFile);
        }
    }
}
