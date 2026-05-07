package com.tinsiag.tinsiagaicodemother.core;

import com.tinsiag.tinsiagaicodemother.ai.AiCodegeneraorService;
import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.ai.model.MultiFileCodeResult;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodegeneraorService aiCodegeneraorService;

    public File generateCodeAndSave(String userPrompt, CodeGenTypeEnum codeGenType){
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (codeGenType){
            case HTML -> generateAndSaveHtmlCode(userPrompt);

            case MULTI_FILE -> generateAndSaveMutilFileCode(userPrompt);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型错误"+codeGenType.getValue());
        };
    }

    private File generateAndSaveMutilFileCode(String userPrompt) {
        MultiFileCodeResult multiFileCodeResult = aiCodegeneraorService.generateMultiFileCode(userPrompt);

        return  CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
    }

    private File generateAndSaveHtmlCode(String userPrompt) {
        HtmlCodeResult htmlCodeResult = aiCodegeneraorService.generateHtmlCode(userPrompt);

        return CodeFileSaver.saveHtmlCode(htmlCodeResult);
    }

}
