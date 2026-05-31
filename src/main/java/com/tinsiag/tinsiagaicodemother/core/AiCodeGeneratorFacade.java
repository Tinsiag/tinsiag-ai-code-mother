package com.tinsiag.tinsiagaicodemother.core;

import com.tinsiag.tinsiagaicodemother.ai.AiCodeGenerateServiceFactory;
import com.tinsiag.tinsiagaicodemother.ai.AiCodegeneraorService;
import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.ai.model.MultiFileCodeResult;
import com.tinsiag.tinsiagaicodemother.core.parser.CodeParserExecutor;
import com.tinsiag.tinsiagaicodemother.core.saver.CodeFileSaverExecutor;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
@Slf4j
@Service
public class AiCodeGeneratorFacade {
    // 注入 AI 代码生成服务，负责根据用户提示词生成 HTML / 多文件代码内容
    @Resource
    private AiCodeGenerateServiceFactory aiCodeGenerateServiceFactory;

    /**
     * 统一入口：根据用户输入的提示词和生成类型，生成代码并保存到本地。
     *
     * @param userPrompt 用户输入的需求描述
     * @param codeGenType 代码生成类型
     * @return 保存结果对应的目录/文件对象
     */
    public File generateCodeAndSave(String userPrompt, CodeGenTypeEnum codeGenType,Long appId){
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodegeneraorService aiCodegeneraorService = aiCodeGenerateServiceFactory.getAiCodeGenerateService(appId,codeGenType);
        return switch (codeGenType){
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodegeneraorService.generateHtmlCode(userPrompt);
                yield  CodeFileSaverExecutor.executor(htmlCodeResult, CodeGenTypeEnum.HTML,appId);

            }

            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodegeneraorService.generateMultiFileCode(userPrompt);
                yield CodeFileSaverExecutor.executor(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        };
    }
    /**
     * 统一入口：根据用户输入的提示词和生成类型，流式生成代码并在完成后保存。
     *
     * @param userPrompt 用户输入的需求描述
     * @param codeGenType 代码生成类型
     * @return 流式输出的代码片段
     */
    public Flux<String> generateCodeAndSaveStream(String userPrompt, CodeGenTypeEnum codeGenType,Long appId){
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodegeneraorService aiCodegeneraorService = aiCodeGenerateServiceFactory.getAiCodeGenerateService(appId,codeGenType);
        return switch (codeGenType){
            case HTML -> {
                Flux<String> codeStream = aiCodegeneraorService.generateHtmlCodeStream(userPrompt);
                // 累积完整响应，便于流结束后进行整体解析
                yield  processCodeStream(codeStream, CodeGenTypeEnum.HTML,appId);
            }

            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodegeneraorService.generateMultiFileCodeStream(userPrompt);
                // 累积完整响应，便于流结束后进行整体解析
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                Flux<String> codeStream = aiCodegeneraorService.generateVueGenProjectSystemPrompt(appId, userPrompt);
                // Vue 工程模式的流式生成通常伴随工具调用，工具调用完成
                yield  processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持类型" + codeGenType.getValue());
            }
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param CodeStream 代码流
     * @param codeGenTypeEnum 代码生成类型
     * @return 原始代码流
     */
    @SuppressWarnings("unused")
    private Flux<String> processCodeStream(Flux<String> CodeStream,CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        // 在流式返回过程中累积完整代码，等完成后再统一解析和保存
        StringBuilder stringBuilder = new StringBuilder();
        return CodeStream.doOnNext(chunk -> {
            stringBuilder.append(chunk);
            // 实时收集代码片段
        }).doOnComplete(() -> {
            // 流式返回完成后，执行解析与保存
            try {
                String completeCode = stringBuilder.toString();
                Object executedParser = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                File saveDir = CodeFileSaverExecutor.executor(executedParser, codeGenTypeEnum, appId);
                log.info("保存文件成功：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存文件失败：{}", e.getMessage());
            }
        });
    }


    /**
     * 非流式生成多文件代码，并直接保存到本地。
     *
     * @param userPrompt 用户输入的需求描述
     * @return 保存后的目录/文件对象
     */
    private File generateAndSaveMutilFileCode(String userPrompt,Long appId) {
        AiCodegeneraorService aiCodegeneraorService = aiCodeGenerateServiceFactory.getAiCodeGenerateService(appId);

        MultiFileCodeResult multiFileCodeResult = aiCodegeneraorService.generateMultiFileCode(userPrompt);

        return  CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
    }

    /**
     * 非流式生成 HTML 代码，并直接保存到本地。
     *
     * @param userPrompt 用户输入的需求描述
     * @return 保存后的目录/文件对象
     */
    private File generateAndSaveHtmlCode(String userPrompt,Long appId) {
        AiCodegeneraorService aiCodegeneraorService = aiCodeGenerateServiceFactory.getAiCodeGenerateService(appId);

        HtmlCodeResult htmlCodeResult = aiCodegeneraorService.generateHtmlCode(userPrompt);

        return CodeFileSaver.saveHtmlCode(htmlCodeResult);
    }

}
