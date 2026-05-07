package com.tinsiag.tinsiagaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.ai.model.MultiFileCodeResult;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {
    /**
     * 文件保存根目录
     */
    private static final String FILE_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";

    /**
     * 保存HTML网页代码
     *
     */
    public static File saveHtmlCode(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue()) ;
        saveFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }
    /**
     * 保存多文件代码
     * @param multiFileCodeResult
     * @return
     */
    public static File saveMultiFileCode(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue()) ;
        saveFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        saveFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        saveFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }


    /**
     * 构建文件的唯一路径 （/tmp/code_output/bizType_雪花 ID）
     * @param bizType
     * @return
     */

    private static String buildUniqueDir(String bizType){
        String UniqueDir = StrUtil.format("{}_{}", bizType , IdUtil.getSnowflakeNextId());
        String dirPath = FILE_ROOT_DIR + File.separator + UniqueDir;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存单个文件
     */
    public static  void saveFile(String dirPath, String fileName, String content) {
        String fullPath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
    }
}
