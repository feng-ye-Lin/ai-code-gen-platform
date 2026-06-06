package com.yuri.yeaicodegenplatform.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuri.yeaicodegenplatform.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yuri.yeaicodegenplatform.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 对话历史 服务层。
 *
 * @author yuri
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息
     *
     * @param appId   应用id
     * @param userId  用户id
     * @param message 消息内容
     * @return 保存的对话历史
     */
    ChatHistory saveUserMessage(Long appId, Long userId, String message);

    /**
     * 保存 AI 消息
     *
     * @param appId   应用id
     * @param userId  用户id
     * @param message 消息内容
     * @return 保存的对话历史
     */
    ChatHistory saveAiMessage(Long appId, Long userId, String message);

    /**
     * 保存错误消息（AI 回复失败时记录）
     *
     * @param appId        应用id
     * @param userId       用户id
     * @param errorMessage 错误信息
     * @return 保存的对话历史
     */
    ChatHistory saveErrorMessage(Long appId, Long userId, String errorMessage);

    /**
     * 根据应用id删除所有对话历史
     *
     * @param appId 应用id
     * @return 删除的记录数
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件
     *
     * @param queryRequest 查询请求参数
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest queryRequest);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}