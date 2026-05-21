package com.tinsiag.tinsiagaicodemother.model.dto.ChatHistory;

import com.tinsiag.tinsiagaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 消息
     */
    private String message;

    /**
     * user/ai
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 游标时间，只查询该时间之前的消息
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 1L;
}