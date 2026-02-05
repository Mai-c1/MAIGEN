package com.maigen.analysis.mq;

import cn.hutool.core.util.StrUtil;
import com.maigen.common.core.enums.TaskStatusEnum;
import com.maigen.common.core.model.dto.TaskErrorDTO;
import com.maigen.common.core.model.dto.TaskStatusDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import com.maigen.analysis.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSubmitConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final AiAnalysisService aiAnalysisService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_TASK_SUBMIT)
    public void receiveTask(Long taskId) {
        log.info("Analysis 模块收到任务消息: taskId={}", taskId);

        try {
            // 1. 发送初始状态：分析中 (10%)
            sendStatusUpdate(taskId, TaskStatusEnum.ANALYZING, 10, "正在拉取数据并准备分析...");

            // 2. 调用 AI 分析与代码生成 (内部会细化更新状态 20%-60%)
            // 此时状态会从 ANALYZING 流转到 GENERATING
            aiAnalysisService.analyzeAndGenerate(taskId);

            // 3. 生成完成，准备验证 (70%)
            sendStatusUpdate(taskId, TaskStatusEnum.VERIFYING, 70, "代码生成完成，正在启动沙箱验证...");

            // 4. 将轻量级任务消息发送给 Sandbox 模块执行
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, 
                    RabbitMQConstants.QUEUE_TASK_EXECUTE, taskId);
            
            log.info("任务已下发至 Sandbox: taskId={}", taskId);

        } catch (Exception e) {
            log.error("处理任务分析失败: taskId={}", taskId, e);
            
            // 截取错误信息，防止过长
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 100) {
                errorMsg = StrUtil.subPre(errorMsg, 100) + "...";
            }
            
            // 发送失败状态更新
            sendStatusUpdate(taskId, TaskStatusEnum.FAILED, "分析生成阶段异常: " + errorMsg);
            
            if (isRetryableException(e)) {
                throw new RuntimeException("Analysis temporary failure, triggering MQ retry", e);
            }
        }
    }

    private boolean isRetryableException(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        msg = msg.toLowerCase();
        return msg.contains("timeout") || msg.contains("connection") || msg.contains("refused");
    }

    private void sendStatusUpdate(Long taskId, TaskStatusEnum status) {
        sendStatusUpdate(taskId, status, status.getDefaultProgress(), status.getDefaultMessage());
    }

    private void sendStatusUpdate(Long taskId, TaskStatusEnum status, String message) {
        sendStatusUpdate(taskId, status, status.getDefaultProgress(), message);
    }

    private void sendStatusUpdate(Long taskId, TaskStatusEnum status, Integer progress, String message) {
        TaskStatusDTO statusDTO = TaskStatusDTO.builder()
                .taskId(taskId)
                .status(status.getCode())
                .progress(progress)
                .message(message)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, 
                RabbitMQConstants.QUEUE_TASK_STATUS, statusDTO);
    }
}
