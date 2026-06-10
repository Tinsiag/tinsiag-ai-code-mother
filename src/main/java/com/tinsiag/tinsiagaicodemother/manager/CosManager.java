package com.tinsiag.tinsiagaicodemother.manager;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.tinsiag.tinsiagaicodemother.config.QiniuKodoConfig;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class CosManager {

    @Resource
    private QiniuKodoConfig qiniuKodoConfig;

    public String upLoadFile(File file, String key) {
        try {
            String uploadQiniuAuth = qiniuKodoConfig.uploadQiniuAuth();
            Response response = qiniuKodoConfig.uploadManager().put(file, key, uploadQiniuAuth);
            if (!response.isOK()) {
                log.error("上传文件失败，状态码：{}，响应内容：{}", response.statusCode, response.bodyString());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");

            }
            return qiniuKodoConfig.getUrl() + key;
        } catch (QiniuException e) {
            log.error("上传文件失败:", e.getMessage());
            return null;
        }

    }
}
