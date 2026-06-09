package com.yuri.yeaicodegenplatform.ai.tools;

import com.yuri.yeaicodegenplatform.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 *
 * @author yuri
 * @create 2026-06-07 22:31
 */
@Slf4j
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件的相对路径") String relativeFilePath, @P("要写入文件的内容") String content, @ToolMemoryId Long appId) {
        Path path = Paths.get(relativeFilePath);
        if (!path.isAbsolute()) {
            // 相对路径处理，创建基于 appId 的项目目录
            String projectDirName = "vue_project_" + appId;
            Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
            path = projectRoot.resolve(relativeFilePath);
        }
        // 创建父目录（如果不存在）
        Path parentDir = path.getParent();
        if (parentDir != null) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                log.error("创建目录失败，目录路径：", parentDir, e);
                return "创建目录失败：" + relativeFilePath + "，错误：" + e.getMessage();
            }
        }
        // 写入文件内容
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件：{}", path.toAbsolutePath());
            // 注：要返回相对路径，不能让 AI 把文件绝对路径返回给用户
            return "文件写入成功：" + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "文件写入失败：" + relativeFilePath + "，错误：" + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
