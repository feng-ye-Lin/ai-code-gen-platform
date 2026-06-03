package com.yuri.yeaicodegenplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yuri
 * @create 2026-06-03
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面（仅管理员可修改）
     */
    private String cover;

    /**
     * 优先级（仅管理员可修改）
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
