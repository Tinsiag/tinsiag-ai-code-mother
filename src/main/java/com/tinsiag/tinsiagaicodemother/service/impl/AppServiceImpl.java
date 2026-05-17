package com.tinsiag.tinsiagaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.tinsiag.tinsiagaicodemother.constant.AppConstant;
import com.tinsiag.tinsiagaicodemother.model.dto.App.AppQueryRequest;
import com.tinsiag.tinsiagaicodemother.model.vo.AppVO;
import com.tinsiag.tinsiagaicodemother.core.AiCodeGeneratorFacade;
import com.tinsiag.tinsiagaicodemother.exception.BusinessException;
import com.tinsiag.tinsiagaicodemother.exception.ErrorCode;
import com.tinsiag.tinsiagaicodemother.exception.ThrowUtils;
import com.tinsiag.tinsiagaicodemother.model.entity.App;
import com.tinsiag.tinsiagaicodemother.mapper.AppMapper;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import com.tinsiag.tinsiagaicodemother.model.enums.CodeGenTypeEnum;
import com.tinsiag.tinsiagaicodemother.model.vo.UserVO;
import com.tinsiag.tinsiagaicodemother.service.AppService;
import com.tinsiag.tinsiagaicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author tinsiag
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{
    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
    if (appQueryRequest == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
    }
    Long id = appQueryRequest.getId();
    String appName = appQueryRequest.getAppName();
    String cover = appQueryRequest.getCover();
    String initPrompt = appQueryRequest.getInitPrompt();
    String codeGenType = appQueryRequest.getCodeGenType();
    String deployKey = appQueryRequest.getDeployKey();
    Integer priority = appQueryRequest.getPriority();
    Long userId = appQueryRequest.getUserId();
    String sortField = appQueryRequest.getSortField();
    String sortOrder = appQueryRequest.getSortOrder();
    return QueryWrapper.create()
            .eq("id", id)
            .like("appName", appName)
            .like("cover", cover)
            .like("initPrompt", initPrompt)
            .eq("codeGenType", codeGenType)
            .eq("deployKey", deployKey)
            .eq("priority", priority)
            .eq("userId", userId)
            .orderBy(sortField, "ascend".equals(sortOrder));
}
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chat2GenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId<=0,ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message),ErrorCode.PARAMS_ERROR,"提示词为空");

        // 2 . 获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");


        //3 权限校验,仅本人可和应用对话

        if(!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限访问");
        }
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());

        //4 获取应用的代码生成类型

        ThrowUtils.throwIf(enumByValue==null,ErrorCode.PARAMS_ERROR,"应用的代码生成类型错误");

        //5 ai生成代码

        return aiCodeGeneratorFacade.generateCodeAndSaveStream(message, enumByValue, appId);


    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //1. 参数校验
        ThrowUtils.throwIf(appId<=0||appId==null,ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.PARAMS_ERROR,"用户未登录");
        //2. 获取应用信息
        App byId = this.getById(appId);
        ThrowUtils.throwIf(byId==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");

        //3.权限校验
        ThrowUtils.throwIf(!byId.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"无权限部署应用");
        // 4. 检查是否有deploykey ，如果没有则生成6位 deployKey  字母加数字
        String deployKey = byId.getDeployKey();
        if (StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        //5.获取代码生成类型， 获取原始代码生成路径
        String codeGenType = byId.getCodeGenType();
        String sourcePath = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+sourcePath;

        //6.检查路径是否存在，
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用路径不存在，请先生成应用");
        }
        //7.部署代码（复制文件到部署目录）
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署应用失败，文件操作异常:"+e.getMessage());
        }
        //8.数据库
        App updateapp = new App();
        updateapp.setId(appId);
        updateapp.setDeployedTime(LocalDateTime.now());
        updateapp.setDeployKey(deployKey);
        boolean updateResult = this.updateById(updateapp);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"部署应用失败，数据库更新异常:");
        }
        //9.返回可访问的URL
        return String.format("%s/%s",AppConstant.CODE_DEPLOY_HOST,deployKey);
    }


}
