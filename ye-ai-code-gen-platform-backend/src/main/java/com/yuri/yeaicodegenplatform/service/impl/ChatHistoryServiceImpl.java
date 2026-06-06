package com.yuri.yeaicodegenplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yuri.yeaicodegenplatform.exception.BusinessException;
import com.yuri.yeaicodegenplatform.exception.ErrorCode;
import com.yuri.yeaicodegenplatform.mapper.ChatHistoryMapper;
import com.yuri.yeaicodegenplatform.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yuri.yeaicodegenplatform.model.entity.ChatHistory;
import com.yuri.yeaicodegenplatform.model.enums.MessageTypeEnum;
import com.yuri.yeaicodegenplatform.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author yuri
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Override
    public ChatHistory saveUserMessage(Long appId, Long userId, String message) {
        ChatHistory chatHistory = buildChatHistory(appId, userId, message, MessageTypeEnum.USER.getValue());
        this.save(chatHistory);
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiMessage(Long appId, Long userId, String message) {
        ChatHistory chatHistory = buildChatHistory(appId, userId, message, MessageTypeEnum.AI.getValue());
        this.save(chatHistory);
        return chatHistory;
    }

    @Override
    public ChatHistory saveErrorMessage(Long appId, Long userId, String errorMessage) {
        // 错误消息也标记为 AI 类型，但内容为错误信息
        ChatHistory chatHistory = buildChatHistory(appId, userId, errorMessage, MessageTypeEnum.AI.getValue());
        this.save(chatHistory);
        return chatHistory;
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id不能为空");
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = queryRequest.getId();
        Long appId = queryRequest.getAppId();
        Long userId = queryRequest.getUserId();
        String messageType = queryRequest.getMessageType();
        String cursor = queryRequest.getCursor();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .eq("appId", appId)
                .eq("userId", userId)
                .eq("messageType", messageType);

        // 游标分页：向前加载更多历史记录（查询 createTime 早于游标值的记录）
        if (StrUtil.isNotBlank(cursor)) {
            queryWrapper.lt("createTime", LocalDateTime.parse(cursor));
        }

        // 默认按创建时间降序（最新的在前）
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createTime", false);
        }

        return queryWrapper;
    }

    /**
     * 构建 ChatHistory 实体
     */
    private ChatHistory buildChatHistory(Long appId, Long userId, String message, String messageType) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(appId);
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType(messageType);
        LocalDateTime now = LocalDateTime.now();
        chatHistory.setCreateTime(now);
        chatHistory.setUpdateTime(now);
        return chatHistory;
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

}