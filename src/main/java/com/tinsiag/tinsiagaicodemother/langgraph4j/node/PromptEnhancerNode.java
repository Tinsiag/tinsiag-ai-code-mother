package com.tinsiag.tinsiagaicodemother.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import com.tinsiag.tinsiagaicodemother.langgraph4j.model.ImageResource;
import com.tinsiag.tinsiagaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state -> {
            WorkflowContext workflowContext = WorkflowContext.getContext(state);
            log.info("执行节点: prompt_enhancer - 增强提示词");
            String originalPrompt = workflowContext.getOriginalPrompt();
            List<ImageResource> imageList = workflowContext.getImageList();
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            enhancedPromptBuilder.append(originalPrompt);
            if (CollUtil.isNotEmpty(imageList)) {
                enhancedPromptBuilder.append("\n\n## 可用素材资源\n");
                enhancedPromptBuilder.append("以下是可用的素材资源列表，请在生成网站中使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\n");
                for (ImageResource image : imageList) {
                    enhancedPromptBuilder.append("- ")
                            .append(image.getCategory().getText())
                            .append("：")
                            .append(image.getDescription())
                            .append("（")
                            .append(image.getUrl())
                            .append("）\n");
                }
            }
            String enhancedPrompt = enhancedPromptBuilder.toString();
            workflowContext.setCurrentStep("提示词增强");
            workflowContext.setEnhancedPrompt(enhancedPrompt);
            log.info("增强后的提示词: {}", enhancedPrompt);
            return WorkflowContext.saveContext(workflowContext);
        });
    }
}
