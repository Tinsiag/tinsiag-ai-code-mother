package com.tinsiag.tinsiagaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.tinsiag.tinsiagaicodemother.model.dto.App.AppQueryRequest;
import com.tinsiag.tinsiagaicodemother.model.vo.AppVO;
import com.tinsiag.tinsiagaicodemother.model.entity.App;
import com.tinsiag.tinsiagaicodemother.model.entity.User;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author tinsiag
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app) ;


    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chat2GenCode(Long appId, String message, User loginUser);

    boolean deleteApp(Long appId);
    /**
     * 网站部署
     *
     */
    String deployApp(Long appId,User loginUser);

    void generateAppScreenshotAsync(Long appId, String appUrl);
}
