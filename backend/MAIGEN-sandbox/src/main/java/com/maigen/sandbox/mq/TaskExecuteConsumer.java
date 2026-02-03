package com.maigen.sandbox.mq;

import com.maigen.common.core.enums.TaskStatusEnum;
import com.maigen.common.core.model.dto.TaskResultDTO;
import com.maigen.common.core.model.dto.TaskStatusDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import com.maigen.sandbox.service.TaskExecuteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExecuteConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TaskExecuteService taskExecuteService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_TASK_EXECUTE)
    public void receiveTask(Long taskId) {
        log.info("Sandbox 模块收到执行任务消息: taskId={}", taskId);

        try {
            // 1. 发送初始状态：验证中 (70%)
            sendStatusUpdate(taskId, TaskStatusEnum.VERIFYING, 70, "正在初始化沙箱隔离环境...");

            // 2. 进入代码执行阶段 (85%)
            sendStatusUpdate(taskId, TaskStatusEnum.VERIFYING, 85, "正在运行代码并验证测试数据...");

            // 3. 调用核心执行逻辑
            TaskResultDTO resultDTO = taskExecuteService.executeTask(taskId);

            // 4. 执行完成，准备结果 (95%)
            sendStatusUpdate(taskId, TaskStatusEnum.VERIFYING, 95, "正在整理验证报告...");

            // 5. 发送最终结果回传
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, 
                    RabbitMQConstants.QUEUE_TASK_UPDATE, resultDTO);
            
            log.info("任务执行结果已回传: taskId={}", taskId);

        } catch (Exception e) {
            log.error("沙箱执行任务失败: taskId={}", taskId, e);
            
            // 发送失败状态更新
            sendStatusUpdate(taskId, TaskStatusEnum.FAILED, "沙箱执行阶段异常: " + e.getMessage());
            
            if (isRetryableException(e)) {
                throw new RuntimeException("Sandbox temporary failure, triggering MQ retry", e);
            }
        }
    }

    private boolean isRetryableException(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        msg = msg.toLowerCase();
        return msg.contains("timeout") || msg.contains("connection") || msg.contains("refused") || msg.contains("500");
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
