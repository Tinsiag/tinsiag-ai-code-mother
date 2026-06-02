package com.tinsiag.tinsiagaicodemother.ai.model.message;

import lombok.Getter;

@Getter
public enum StreamMessageTypeEnum {
    AI_RESPONSE("AI_RESPONSE","AI响应"),
    TOOL_REQUEST("TOOL_REQUEST","工具请求"),
    TOOL_EXECUTED("TOOL_EXECUTED","工具执行");

    private final String value;
    private final String text;

    StreamMessageTypeEnum(String value, String text){
        this.value = value;
        this.text = text;
    }
    public static StreamMessageTypeEnum getByValue(String value){
        for (StreamMessageTypeEnum streamMessageTypeEnum : StreamMessageTypeEnum.values()){
            if (streamMessageTypeEnum.value.equals(value)){
                return streamMessageTypeEnum;
            }
        }
        return null;
    }
}
