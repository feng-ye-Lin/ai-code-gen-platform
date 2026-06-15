package com.yuri.yeaicodegenplatform.langgraph4j.node.concurrent;

import com.yuri.yeaicodegenplatform.ai.tools.MermaidDiagramTool;
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
 * 架构图绘制节点
 * @author yuri
 * @create 2026-06-15 22:58
 */
@Slf4j
public class DiagramCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：架构图绘制节点，time：{}", LocalDateTime.now());

            List<ImageResource> diagrams = new ArrayList<>();
            try {
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                if (plan != null && plan.getDiagramTasks() != null) {
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    log.info("开始并发生成架构图，任务数: {}", plan.getDiagramTasks().size());
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        List<ImageResource> images = diagramTool.generateMermaidDiagram(
                                task.mermaidCode(), task.description());
                        if (images != null) {
                            diagrams.addAll(images);
                        }
                    }
                    log.info("架构图生成完成，共生成 {} 张图片", diagrams.size());
                }
            } catch (Exception e) {
                log.error("架构图生成失败: {}", e.getMessage(), e);
            }

            context.setDiagrams(diagrams);
            context.setCurrentStep("架构图生成");

            return WorkflowContext.saveContext(context);
        });
    }
}
