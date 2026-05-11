package com.tinsiag.tinsiagaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;


/**
 * 抽象代码文件保存器模板类
 */

public abstract class CodeFileSaverTemplate<T> {
    private static final String FILE_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";

    public final File saveCode(T result){
        // 1. 验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath  = buildUniqueDir();
        // 3. 保存文件（具体实现交给子类）
        saveFiles(result, baseDirPath);

        //4. 返回文件目录对象
        return new File(baseDirPath);

    }

    /**
     * 保存单个文件
     */
    public final static  void saveFile(String dirPath, String fileName, String content) {
        if(StrUtil.isNotBlank(content)){
            String fullPath = dirPath + File.separator + fileName;
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
        }

    }

    /**
     * 验证输入参数
     * @param result
     */
    protected void validateInput(T result) {
        if (result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码结果对象不能为空");
        }
    }

    /**
     * 保存文件
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result , String baseDirPath);
    /**
     * 构建文件的唯一路径 （/tmp/code_output/bizType_雪花 ID）
     * @param
     * @return
     */

    protected String buildUniqueDir(){
        String bizType = getCodeType().getValue();
        String UniqueDir = StrUtil.format("{}_{}", bizType , IdUtil.getSnowflakeNextId());
        String dirPath = FILE_ROOT_DIR + File.separator + UniqueDir;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    protected abstract CodeGenTypeEnum getCodeType();
}
