package com.tinsiag.tinsiagaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
/**
 * 多文件代码结果
 */
@Description("生成多文件代码结果")
@Data
public class MultiFileCodeResult {
    @Description("html代码")
    private String htmlCode;
    @Description("css代码")
    private String cssCode;
    @Description("js代码")
    private String jsCode;
    @Description("生成代码描述")
    private String description;
}
