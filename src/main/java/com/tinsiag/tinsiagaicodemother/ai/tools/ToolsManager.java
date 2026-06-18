package com.tinsiag.tinsiagaicodemother.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolsManager {
    /**
     *  工具名称到工具实例的映射
     */
    private final Map<String, BaseTool> toolMap = new HashMap<>();

    @Resource
    private BaseTool[] baseTool;

    /**
     * 初始化工具映射
     */
    @PostConstruct
    public void initTools() {
        for (BaseTool tool : baseTool) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {} ({})", tool.getDisplayName(), tool.getToolName());
        }
        log.info("工具注册完成，共 {} 个工具", toolMap.size());
    }

    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    public BaseTool[]  getAllTool() {
        return baseTool;
    }
}
