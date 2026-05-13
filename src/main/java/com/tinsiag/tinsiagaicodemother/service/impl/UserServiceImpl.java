package com.tinsiag.tinsiagaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.mapper.UserMapper;
import com.tinsiag.tinsiagaicodemother.model.dto.User.UserQueryRequest;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.model.enums.UserRoleEnum;
import com.tinsiag.tinsiagaicodemother.model.vo.LoginUserVO;
import com.tinsiag.tinsiagaicodemother.model.vo.UserVO;
import com.tinsiag.tinsiagaicodemother.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.tinsiag.tinsiagaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author tinsiag
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2.查询用户是否已经存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        // 3. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 4.创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败,数据库错误");
        }

        return user.getId();

    }

    /**
     * 加密
     * @param userPassword
     * @return
     */

    @Override
    public String getEncryptPassword(String userPassword){
        //盐值加密
        final String SALT = "tinsiag";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 用户登录VO
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user){
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //1. 校验参数
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        //2 加密
        String encryptPassword = getEncryptPassword(userPassword);

        //3 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (user.getIsDelete() == 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //4 如果用户存在，记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;

    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询当前用户信息
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;


    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList
     * @return
     */

    @Override
    public List<UserVO> listUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).toList();
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();

        // 1. 处理 ID：前端没传时可能默认为 0，所以除了判 null 还要大于 0 才生效
        queryWrapper.eq("id", id, id != null && id > 0);

        // 2. 处理字符串：使用 isNotBlank() 确保它不是 null，不是 ""，也不是一堆空格
        // 只有当 isNotBlank 返回 true 时，这个条件才会被加进 SQL
        queryWrapper.eq("userRole", userRole, StringUtils.isNotBlank(userRole));
        queryWrapper.like("userAccount", userAccount, StringUtils.isNotBlank(userAccount));
        queryWrapper.like("userName", userName, StringUtils.isNotBlank(userName));
        queryWrapper.like("userProfile", userProfile, StringUtils.isNotBlank(userProfile));

        // 3. 处理排序：必须确保前端真的传了 sortField，否则 orderBy() 拼接空字段会报 SQL 语法错误
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }

        return queryWrapper;
    }
}
