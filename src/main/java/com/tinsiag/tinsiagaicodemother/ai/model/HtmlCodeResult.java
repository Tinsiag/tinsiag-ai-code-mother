package com.tinsiag.tinsiagaicodemother.ai.model;


import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * html代码结果
 */
@Data
@Description("生成HTML代码文件结果")
public class HtmlCodeResult {
    @Description("HTML代码")
    private String htmlCode;
    @Description("代码描述")
    private String description;

}
