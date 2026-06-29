package com.tinsiag.tinsiagaicodemother.langgraph4j.node;

import cn.hutool.core.util.RandomUtil;
import com.tinsiag.tinsiagaicodemother.core.AiCodeGeneratorFacade;
import com.tinsiag.tinsiagaicodemother.langgraph4j.state.WorkflowContext;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import com.tinsiag.tinsiagaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            String enhancedPrompt = context.getEnhancedPrompt();
            CodeGenTypeEnum generationType = context.getGenerationType();
            log.info("开始代码生成，类型: {}, 提示词: {}", generationType.getText(), enhancedPrompt);
            //TODO :先使用指定appId
            long appId = RandomUtil.randomLong();
            Flux<String> codeAndSaveStream = aiCodeGeneratorFacade.generateCodeAndSaveStream(enhancedPrompt, generationType, appId);
            codeAndSaveStream.blockLast(Duration.ofMinutes(10));
            String generatedCodeDir = String.format("%s/%s_%s", enhancedPrompt, generationType, appId);
            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
