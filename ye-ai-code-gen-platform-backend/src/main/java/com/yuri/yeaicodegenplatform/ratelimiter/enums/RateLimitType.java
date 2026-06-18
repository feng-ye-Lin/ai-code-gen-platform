package com.yuri.yeaicodegenplatform.ratelimiter.enums;

/**
 * 限流类型枚举类
 * @author yuri
 * @create 2026-06-17 23:21
 */
public enum RateLimitType {
    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP
}
