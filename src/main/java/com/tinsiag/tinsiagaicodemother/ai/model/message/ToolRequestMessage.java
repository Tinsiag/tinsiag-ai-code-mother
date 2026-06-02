package com.tinsiag.tinsiagaicodemother.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.PartialToolCall;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ToolRequestMessage extends StreamMessage{
    private Integer index;
    private String id;
    private String name;
    private String arguments;

    public ToolRequestMessage() {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
    }

    public ToolRequestMessage(PartialToolCall partialToolCall) {
        this();
        this.index = partialToolCall.index();
        this.id = partialToolCall.id();
        this.name = partialToolCall.name();
        this.arguments = partialToolCall.partialArguments();
    }

    public ToolRequestMessage(ToolExecutionRequest toolExecutionRequest) {
        this();
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }

}
