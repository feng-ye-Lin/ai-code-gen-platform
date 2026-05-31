package com.yuri.yeaicodegenplatform.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yuri
 * @create 2026-05-31 12:37
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
