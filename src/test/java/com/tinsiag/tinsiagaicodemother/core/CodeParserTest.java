package com.tinsiag.tinsiagaicodemother.core;

import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;
import com.tinsiag.tinsiagaicodemother.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>
                ```
                
                随便写一段描述
                """;

        HtmlCodeResult result = CodeParser.parseHtmlCode(codeContent);

        assertNotNull(result);
        assertNotNull(result.getHtmlCode());

        // 确认真正提取出了 HTML，而不是把整段文本都当成 HTML
        assertTrue(result.getHtmlCode().startsWith("<!DOCTYPE html>"));
        assertTrue(result.getHtmlCode().contains("<title>测试页面</title>"));
        assertTrue(result.getHtmlCode().contains("<h1>Hello World!</h1>"));

        // 确认描述文字没有被提取进去
        assertFalse(result.getHtmlCode().contains("随便写一段描述"));
        assertFalse(result.getHtmlCode().contains("```html"));
        assertFalse(result.getHtmlCode().contains("```"));
    }

    @Test
    void parseMultiFileCode() {
        String codeContent = """
                创建一个完整的网页：
                
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>多文件示例</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>欢迎使用</h1>
                    <script src="script.js"></script>
                </body>
                </html>
                ```
                
                ```css
                h1 {
                    color: blue;
                    text-align: center;
                }
                ```
                
                ```js
                console.log('页面加载完成');
                ```
                
                文件创建完成！
                """;

        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeContent);

        assertNotNull(result);

        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());

        // 检查 HTML 是否正确提取
        assertTrue(result.getHtmlCode().startsWith("<!DOCTYPE html>"));
        assertTrue(result.getHtmlCode().contains("<title>多文件示例</title>"));
        assertTrue(result.getHtmlCode().contains("<h1>欢迎使用</h1>"));

        // 检查 CSS 是否正确提取
        assertTrue(result.getCssCode().contains("color: blue"));
        assertTrue(result.getCssCode().contains("text-align: center"));

        // 检查 JS 是否正确提取
        assertTrue(result.getJsCode().contains("console.log('页面加载完成');"));

        // 确认代码块标记没有被提取进去
        assertFalse(result.getHtmlCode().contains("```html"));
        assertFalse(result.getCssCode().contains("```css"));
        assertFalse(result.getJsCode().contains("```js"));
        assertFalse(result.getHtmlCode().contains("创建一个完整的网页"));
        assertFalse(result.getJsCode().contains("文件创建完成"));
    }
}