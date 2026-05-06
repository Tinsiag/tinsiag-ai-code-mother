package com.tinsiag.tinsiagaicodemother.exception;


public class ThrowUtils {

    /**
     * 如果条件满足，则抛出
     * @param condition
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }
    public static void throwIf(boolean condition, ErrorCode errorCode, String message){
        throwIf(condition, new BusinessException(errorCode,message));
    }
    public static void throwIf(boolean condition,ErrorCode errorCode){
        throwIf(condition, new BusinessException(errorCode));
    }
}
