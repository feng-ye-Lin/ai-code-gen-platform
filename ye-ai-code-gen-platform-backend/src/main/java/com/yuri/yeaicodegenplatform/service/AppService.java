package com.yuri.yeaicodegenplatform.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuri.yeaicodegenplatform.model.dto.app.AppQueryRequest;
import com.yuri.yeaicodegenplatform.model.entity.App;
import com.yuri.yeaicodegenplatform.model.entity.User;
import com.yuri.yeaicodegenplatform.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author yuri
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用视图对象
     *
     * @param app 应用
     * @return AppVO
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用列表
     * @return AppVO 列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求参数
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);

    /**
     * 删除应用及其关联的对话历史
     *
     * @param appId 应用id
     * @return 删除结果
     */
    boolean deleteAppById(Long appId);
}
