package com.yuri.yeaicodegenplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yuri
 * @create 2026-06-03
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt（必填）
     */
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    private static final long serialVersionUID = 1L;
}
