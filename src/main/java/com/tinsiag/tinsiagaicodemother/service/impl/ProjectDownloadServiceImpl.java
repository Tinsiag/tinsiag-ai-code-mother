package com.tinsiag.tinsiagaicodemother.service.impl;

import cn.hutool.core.util.ZipUtil;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.exception.ThrowUtils;
import com.tinsiag.tinsiagaicodemother.service.ProjectDownloadService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".bak",
            ".cache"
    );

    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse httpServletResponse) {
        // 基础校验
        ThrowUtils.throwIf(StringUtils.isBlank(projectPath), ErrorCode.PARAMS_ERROR,"项目路径不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR,"下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists() || !projectDir.isDirectory(), ErrorCode.PARAMS_ERROR,"项目路径无效");
        log.info("准备打包下载项目，路径：{}，下载文件名：{}", projectPath, downloadFileName);
        // 设置http头
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setContentType("application/zip");
        httpServletResponse.setHeader("Content-Disposition",
                String.format("attachment; filename=\"%s\"", downloadFileName));
        // 定义文件过滤器
        FileFilter filter =  file -> isPathAllowed(projectDir.toPath(), file.toPath());

        // 打包
        try {
            ZipUtil.zip(httpServletResponse.getOutputStream(), StandardCharsets.UTF_8,false,filter,projectDir);
            log.info("项目打包下载成功，路径：{}，下载文件名：{}", projectPath, downloadFileName);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"打包下载项目失败+" + e.getMessage());
        }

    }


    private boolean isPathAllowed(Path projectRoot, Path fullPath){
        /**
         * projectRoot = C:\Users\asus\project
         * fullPath    = C:\Users\asus\project\src\main\App.java
         * projectRoot.relativize(fullPath) -> src\main\App.java
         */
        Path relativizePath = projectRoot.relativize(fullPath);
        //检查路径中的每一部分是否符合要求
        for(Path path:relativizePath){
            String partName = path.toString();
            //检查是否在忽略的名称列表中
            if(IGNORED_NAMES.contains(partName)){
                return false;
            }
            if(IGNORED_EXTENSIONS.stream().anyMatch(ext->partName.toLowerCase().endsWith(ext))){
                return false;
            }
        }
        return true ;
    }
}
