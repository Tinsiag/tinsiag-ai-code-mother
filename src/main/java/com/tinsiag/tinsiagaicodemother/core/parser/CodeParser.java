package com.tinsiag.tinsiagaicodemother.core.parser;

import com.tinsiag.tinsiagaicodemother.ai.model.HtmlCodeResult;

public interface CodeParser <T>{
    T parseCode(String codeContent);

}
