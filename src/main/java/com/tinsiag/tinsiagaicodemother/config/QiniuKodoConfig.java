package com.tinsiag.tinsiagaicodemother.config;

import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "qiniu")
@Configuration
public class QiniuKodoConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String regionId;
    private boolean useHttpsDomains;
    private String domain;
    private boolean useHttpsCDN;

    public String uploadQiniuAuth() {
        return Auth.create(accessKey, secretKey).uploadToken(bucket);

    }

    @Bean
    public UploadManager uploadManager() {
        com.qiniu.storage.Configuration cfg = com.qiniu.storage.Configuration.create(Region.createWithRegionId(regionId));
        cfg.resumableUploadAPIVersion = com.qiniu.storage.Configuration.ResumableUploadAPIVersion.V2;
        cfg.useHttpsDomains = useHttpsDomains;
        return new UploadManager(cfg);
    }

    public String getUrl(){
        String protocol = useHttpsCDN ? "https://" : "http://";
        return protocol + domain + "/";
    }




}
