package com.tinsiag.tinsiagaicodemother.ai;

import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;


public interface AiCodeGenTypeRoutingService {

    @SystemMessage(fromResource = "prompt/codegen-router-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
