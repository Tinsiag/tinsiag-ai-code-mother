package com.tinsiag.tinsiagaicodemother.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.tinsiag.tinsiagaicodemother.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
public class FileWriteTool extends BaseTool{

    @Tool("写入文件到指定目录")
    public String writeFile(@P("文件相对路径") String relativePath, @P("要写入的文件的内容") String content, @ToolMemoryId Long appId){
        try {
            Path path = Paths.get(relativePath);
            if(!path.isAbsolute()){
                String projectPath = "vue_project_" + String.valueOf(appId);
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectPath);
                path = projectRoot.resolve(path);
            }
            Path pathParent = path.getParent();
            // 创建父目录
            if(pathParent != null){
                Files.createDirectories(pathParent);
            }
            // 写入文件

            Files.write(path,content.getBytes(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            log.info("文件写入成功，路径：{}", path.toAbsolutePath());
            return "写入文件成功:" + relativePath;
        } catch (Exception e) {
            log.error("文件写入失败，路径：{}，错误信息：{}", relativePath, e.getMessage());
            return "文件写入失败：" + e.getMessage();
        }
    }



    // 核心方法不变，此处省略

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                        [🔧工具调用] %s %s
                        ```%s
                        %s
                        ```
                        """, getDisplayName(), relativeFilePath, suffix, content);
    }
}


