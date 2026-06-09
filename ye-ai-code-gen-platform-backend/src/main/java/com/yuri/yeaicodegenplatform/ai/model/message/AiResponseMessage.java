package com.yuri.yeaicodegenplatform.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AI 响应消息
 * @author yuri
 * @create 2026-06-07 23:35
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AiResponseMessage extends StreamMessage {
    private String data;

    public AiResponseMessage(String data) {
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        this.data = data;
    }
}
