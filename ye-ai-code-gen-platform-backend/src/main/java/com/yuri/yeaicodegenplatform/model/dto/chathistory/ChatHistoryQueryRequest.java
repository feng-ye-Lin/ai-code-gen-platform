package com.yuri.yeaicodegenplatform.model.dto.chathistory;

import com.yuri.yeaicodegenplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 对话历史查询请求
 *
 * @author yuri
 * @create 2026-06-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 游标（用于向前加载更多历史记录，传上次查询结果中最小的 createTime）
     */
    private String cursor;

    private static final long serialVersionUID = 1L;
}