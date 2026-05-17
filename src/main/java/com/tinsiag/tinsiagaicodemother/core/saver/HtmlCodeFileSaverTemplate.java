package com.tinsiag.tinsiagaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;

public class HtmlCodeFileSaverTemplate  extends CodeFileSaverTemplate<HtmlCodeResult>  {

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        saveFile(baseDirPath,"index.html",result.getHtmlCode());
    }



    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML ;
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML 代码不能为空");
        }

    }
}
