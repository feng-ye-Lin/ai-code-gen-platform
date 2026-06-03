package com.yuri.yeaicodegenplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yuri
 * @create 2026-06-03 23:19
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}

