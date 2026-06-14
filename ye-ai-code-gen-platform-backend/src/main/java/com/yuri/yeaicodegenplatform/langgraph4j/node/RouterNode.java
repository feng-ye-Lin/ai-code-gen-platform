package com.yuri.yeaicodegenplatform.langgraph4j.node;

import com.yuri.yeaicodegenplatform.ai.AiCodeGenTypeRoutingService;
import com.yuri.yeaicodegenplatform.ai.model.enums.CodeGenTypeEnum;
import com.yuri.yeaicodegenplatform.langgraph4j.state.WorkflowContext;
import com.yuri.yeaicodegenplatform.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

/**
 * 智能路由节点
 *
 * @author yuri
 * @create 2026-06-14 16:02
 */
@Slf4j
public class RouterNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return AsyncNodeAction.node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：智能路由");

            CodeGenTypeEnum generationType;
            try {
                // 获取 AI 路由服务
                AiCodeGenTypeRoutingService routingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                // 根据原始提示词进行智能路由
                generationType = routingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("AI 智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI 智能路由失败，使用默认 HTML 类型: {}", e.getMessage());
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型：{}", generationType.getText());

            return WorkflowContext.saveContext(context);
        });
    }
}
