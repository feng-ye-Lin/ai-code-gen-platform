package com.yuri.yeaicodegenplatform.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author yuri
 * @create 2026-06-11 21:38
 */
public interface ProjectDownloadService {

    /**
     * 下载项目为 zip 文件
     * @param projectPath 项目路径
     * @param downloadFileName 下载文件名
     * @param response 响应
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
