package com.tinsiag.tinsiagaicodemother.langgraph4j.node;

import com.tinsiag.tinsiagaicodemother.langgraph4j.ai.ImageCollectionService;
import com.tinsiag.tinsiagaicodemother.langgraph4j.model.ImageResource;
import com.tinsiag.tinsiagaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import com.tinsiag.tinsiagaicodemother.langgraph4j.state.WorkflowContext;

import com.tinsiag.tinsiagaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: image_collector - 收集图片素材");
            String originalPrompt = context.getOriginalPrompt();
            List<ImageResource> imageResourceList = List.of();
            try {
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                imageResourceList = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败，使用空图片素材列表", e);
            }
            context.setCurrentStep("图片收集");
            context.setImageList(imageResourceList);
            log.info("收集到图片素材: {}", imageResourceList);
            return WorkflowContext.saveContext(context);
        });
    }

}
