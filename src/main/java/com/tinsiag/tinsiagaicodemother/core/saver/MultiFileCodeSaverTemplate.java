package com.tinsiag.tinsiagaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.ai.model.MultiFileCodeResult;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;

public class  MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        saveFile(baseDirPath, "index.html", result.getHtmlCode());
        saveFile(baseDirPath, "style.css", result.getCssCode());
        saveFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if(StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML 代码不能为空");
        }
    }
}
