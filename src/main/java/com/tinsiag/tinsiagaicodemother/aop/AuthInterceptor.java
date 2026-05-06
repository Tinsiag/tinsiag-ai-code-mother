package com.tinsiag.tinsiagaicodemother.aop;

import com.tinsiag.tinsiagaicodemother.annotation.AuthCheck;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.model.enums.UserRoleEnum;
import com.tinsiag.tinsiagaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint , AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 如果不需要权限，直接放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //校验权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //没有权限 直接拒绝
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须要有管理员权限
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum)&& !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);

        }
        return joinPoint.proceed();
    }
}
