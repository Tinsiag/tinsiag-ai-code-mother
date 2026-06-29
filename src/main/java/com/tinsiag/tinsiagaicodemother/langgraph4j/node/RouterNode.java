package com.tinsiag.tinsiagaicodemother.langgraph4j.node;

import com.tinsiag.tinsiagaicodemother.ai.AiCodeGenTypeRoutingService;
import com.tinsiag.tinsiagaicodemother.langgraph4j.state.WorkflowContext;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import com.tinsiag.tinsiagaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");
            CodeGenTypeEnum codeGenTypeEnum = null;
            try {
                AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("路由决策完成，选择类型: {}", codeGenTypeEnum.getText());
            } catch (Exception e) {
                log.error("路由决策失败，使用默认类型: {}", CodeGenTypeEnum.HTML.getText(), e);
                codeGenTypeEnum = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(codeGenTypeEnum);
            log.info("路由决策完成，选择类型: {}", codeGenTypeEnum.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}
