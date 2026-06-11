package com.yuri.yeaicodegenplatform.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuri.yeaicodegenplatform.model.dto.app.AppAddRequest;
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
     * 创建应用
     *
     * @param appAddRequest 应用创建请求
     * @param loginUser     登录用户
     * @return 应用 ID
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);

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

    /**
     * 应用下载次数
     *
     * @param appId 应用id
     */
    void incrementDownloadCount(Long appId);
}
