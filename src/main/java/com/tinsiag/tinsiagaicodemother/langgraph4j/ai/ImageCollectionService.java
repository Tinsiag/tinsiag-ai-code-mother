package com.tinsiag.tinsiagaicodemother.langgraph4j.ai;

import com.tinsiag.tinsiagaicodemother.langgraph4j.model.ImageResource;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface ImageCollectionService {

    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    List<ImageResource> collectImages(@UserMessage String userPrompt);
}
