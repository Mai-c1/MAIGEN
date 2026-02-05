package com.maigen.api.mq;

import com.maigen.api.entity.TaskExecutionLog;
import com.maigen.api.service.TaskExecutionLogService;
import com.maigen.common.core.model.dto.TaskExecutionLogDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskLogConsumer {

    private final TaskExecutionLogService logService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_TASK_LOG)
    public void receiveLog(TaskExecutionLogDTO logDTO) {
        log.info("收到任务执行日志: taskId={}, step={}", logDTO.getTaskId(), logDTO.getStepOrder());
        
        try {
            TaskExecutionLog entity = new TaskExecutionLog();
            entity.setTaskId(logDTO.getTaskId());
            entity.setStepOrder(logDTO.getStepOrder());
            entity.setRoleName(logDTO.getRoleName());
            entity.setPromptSnapshot(logDTO.getPromptSnapshot());
            entity.setAiResponse(logDTO.getAiResponse());
            entity.setCreateTime(logDTO.getCreateTime());
            
            logService.save(entity);
        } catch (Exception e) {
            log.error("保存任务日志失败: {}", e.getMessage(), e);
        }
    }
}
