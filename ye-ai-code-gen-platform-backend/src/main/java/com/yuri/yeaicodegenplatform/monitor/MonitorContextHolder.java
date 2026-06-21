package com.yuri.yeaicodegenplatform.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yuri
 * @create 2026-06-20 23:46
 */
@Slf4j
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
        log.info("设置监控上下文: {}", context);
    }

    /**
     * 获取当前监控上下文
     */
    public static MonitorContext getContext() {
        MonitorContext context = CONTEXT_HOLDER.get();
        log.info("获取监控上下文: {}", context);
        return context;
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
        log.info("清除监控上下文");
    }
}
