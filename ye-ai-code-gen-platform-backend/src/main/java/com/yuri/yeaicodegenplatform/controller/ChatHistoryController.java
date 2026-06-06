package com.yuri.yeaicodegenplatform.controller;

import com.mybatisflex.core.paginate.Page;
import com.yuri.yeaicodegenplatform.annotation.AuthCheck;
import com.yuri.yeaicodegenplatform.common.BaseResponse;
import com.yuri.yeaicodegenplatform.common.ResultUtils;
import com.yuri.yeaicodegenplatform.constant.UserConstant;
import com.yuri.yeaicodegenplatform.exception.BusinessException;
import com.yuri.yeaicodegenplatform.exception.ErrorCode;
import com.yuri.yeaicodegenplatform.exception.ThrowUtils;
import com.yuri.yeaicodegenplatform.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yuri.yeaicodegenplatform.model.entity.App;
import com.yuri.yeaicodegenplatform.model.entity.ChatHistory;
import com.yuri.yeaicodegenplatform.model.entity.User;
import com.yuri.yeaicodegenplatform.service.AppService;
import com.yuri.yeaicodegenplatform.service.ChatHistoryService;
import com.yuri.yeaicodegenplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author yuri
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    /**
     * 分页查询某个应用的对话历史（仅应用创建者和管理员可见）
     * 每次加载最新 10 条消息，支持游标分页向前加载更多历史记录
     *
     * @param queryRequest 查询请求
     * @param request      请求
     * @return 分页对话历史
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPage(@RequestBody ChatHistoryQueryRequest queryRequest,
                                                                  HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = queryRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 验证应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 仅应用创建者和管理员可见
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        if (!isCreator && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        // 限制每页最多 10 条
        ThrowUtils.throwIf(pageSize > 10, ErrorCode.PARAMS_ERROR, "每页最多 10 条");

        Page<ChatHistory> page = chatHistoryService.page(Page.of(pageNum, pageSize),
                chatHistoryService.getQueryWrapper(queryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 管理员分页查询所有对话历史（按时间降序排序，便于内容监管）
     *
     * @param queryRequest 查询请求
     * @return 分页对话历史
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listChatHistoryByAdmin(@RequestBody ChatHistoryQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<ChatHistory> page = chatHistoryService.page(Page.of(pageNum, pageSize),
                chatHistoryService.getQueryWrapper(queryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 根据应用id删除所有对话历史（管理员专用）
     *
     * @param appId 应用id
     * @return 删除结果
     */
    @DeleteMapping("/admin/delete/{appId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteByAppIdByAdmin(@PathVariable Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        boolean result = chatHistoryService.deleteByAppId(appId);
        return ResultUtils.success(result);
    }

    /**
     * 查询某个应用是否有对话历史
     * 前端用于判断是否展示历史记录还是发送初始化提示词
     *
     * @param appId   应用id
     * @param request 请求
     * @return 是否存在对话历史
     */
    @GetMapping("/has/history")
    public BaseResponse<Boolean> hasHistory(@RequestParam Long appId, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");

        User loginUser = userService.getLoginUser(request);

        // 验证应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 仅应用创建者和管理员可查看
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        if (!isCreator && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(1);
        Page<ChatHistory> page = chatHistoryService.page(Page.of(1, 1),
                chatHistoryService.getQueryWrapper(queryRequest));
        return ResultUtils.success(page.getTotalRow() > 0);
    }
}