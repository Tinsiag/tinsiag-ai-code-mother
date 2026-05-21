package com.tinsiag.tinsiagaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.tinsiag.tinsiagaicodemother.model.dto.ChatHistory.ChatHistoryQueryRequest;
import com.tinsiag.tinsiagaicodemother.model.entity.ChatHistory;
import com.tinsiag.tinsiagaicodemother.model.entity.User;

/**
 * 对话历史 服务层。
 *
 * @author tinsiag
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    boolean addChatMessage(Long appId, String message, String messageType, User loginUser);

    boolean deleteByAppId(Long appId);
}