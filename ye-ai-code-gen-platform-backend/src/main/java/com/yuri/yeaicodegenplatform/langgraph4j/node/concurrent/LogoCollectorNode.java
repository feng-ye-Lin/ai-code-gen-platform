package com.yuri.yeaicodegenplatform.langgraph4j.node.concurrent;

import com.yuri.yeaicodegenplatform.ai.tools.LogoGeneratorTool;
import com.yuri.yeaicodegenplatform.langgraph4j.model.ImageCollectionPlan;
import com.yuri.yeaicodegenplatform.langgraph4j.model.ImageResource;
import com.yuri.yeaicodegenplatform.langgraph4j.state.WorkflowContext;
import com.yuri.yeaicodegenplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Logo 图片收集节点
 *
 * @author yuri
 * @create 2026-06-15 22:59
 */
@Slf4j
public class LogoCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：Logo 图片收集节点，time：{}", LocalDateTime.now());

            List<ImageResource> logos = new ArrayList<>();
            try {
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                if (plan != null && plan.getLogoTasks() != null) {
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    log.info("开始并发生成Logo，任务数: {}", plan.getLogoTasks().size());
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        List<ImageResource> images = logoTool.generateLogos(task.description());
                        if (images != null) {
                            logos.addAll(images);
                        }
                    }
                    log.info("Logo生成完成，共生成 {} 张图片", logos.size());
                }
            } catch (Exception e) {
                log.error("Logo生成失败: {}", e.getMessage(), e);
            }

            context.setLogos(logos);
            context.setCurrentStep("Logo生成");

            return WorkflowContext.saveContext(context);
        });
    }
}
