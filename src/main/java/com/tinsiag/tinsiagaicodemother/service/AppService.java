package com.tinsiag.tinsiagaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.tinsiag.tinsiagaicodemother.common.AppQueryRequest;
import com.tinsiag.tinsiagaicodemother.common.AppVO;
import com.tinsiag.tinsiagaicodemother.model.entity.App;

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
}
