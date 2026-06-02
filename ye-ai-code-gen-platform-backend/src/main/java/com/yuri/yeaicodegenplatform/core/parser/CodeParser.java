package com.yuri.yeaicodegenplatform.core.parser;

/**
 * 代码解析器策略接口
 * @author yuri
 * @create 2026-06-03 1:54
 */
public interface CodeParser<T> {
    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
