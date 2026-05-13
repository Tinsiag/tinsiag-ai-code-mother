package com.tinsiag.tinsiagaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.tinsiag.tinsiagaicodemother.model.dto.User.UserQueryRequest;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.model.vo.LoginUserVO;
import com.tinsiag.tinsiagaicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author tinsiag
 */
public interface UserService extends IService<User> {
    long userRegister(String userAccount, String userPassword, String checkPassword );

    /**
     * 加密
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);
    /**
     * 获取当前登录用户
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
    /**
     * 用户注销
     * @param request
     * @return 返回true/ false
     */

    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest  request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList
     * @return
     */
    List<UserVO> listUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
