package com.tinsiag.tinsiagaicodemother.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.tinsiag.tinsiagaicodemother.annotation.AuthCheck;
import com.tinsiag.tinsiagaicodemother.common.BaseResponse;
import com.tinsiag.tinsiagaicodemother.common.ResultUtils;
import com.tinsiag.tinsiagaicodemother.constant.UserConstant;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.exception.ThrowUtils;
import com.tinsiag.tinsiagaicodemother.model.dto.ChatHistory.ChatHistoryQueryRequest;
import com.tinsiag.tinsiagaicodemother.model.entity.App;
import com.tinsiag.tinsiagaicodemother.model.entity.ChatHistory;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.service.AppService;
import com.tinsiag.tinsiagaicodemother.service.ChatHistoryService;
import com.tinsiag.tinsiagaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史 控制层。
 *
 * @author tinsiag
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 分页获取某个应用的对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @param request                 请求
     * @return 对话历史
     */
    @PostMapping("/app/list/page")
    public BaseResponse<Page<ChatHistory>> listAppChatHistoryByPage(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
                                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = chatHistoryQueryRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long pageSize = chatHistoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 10, ErrorCode.PARAMS_ERROR, "每次最多查询 10 条对话历史");
        chatHistoryQueryRequest.setSortField("createTime");
        chatHistoryQueryRequest.setSortOrder("descend");
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(chatHistoryQueryRequest.getPageNum(), pageSize), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 管理员分页获取对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPageByAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        chatHistoryQueryRequest.setSortField("createTime");
        chatHistoryQueryRequest.setSortOrder("descend");
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(chatHistoryQueryRequest.getPageNum(), chatHistoryQueryRequest.getPageSize()), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }
}