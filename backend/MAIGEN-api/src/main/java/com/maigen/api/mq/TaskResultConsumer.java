package com.maigen.api.mq;

import com.maigen.api.service.TaskService;
import com.maigen.common.core.model.dto.TaskResultDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskResultConsumer {

    private final TaskService taskService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_TASK_UPDATE)
    public void receiveTaskResult(TaskResultDTO resultDTO) {
        log.info("收到沙箱任务结果: taskId={}, success={}", resultDTO.getTaskId(), resultDTO.isSuccess());
        try {
            taskService.handleTaskResult(resultDTO);
        } catch (Exception e) {
            log.error("处理任务结果失败", e);
            // 可以在这里做重试逻辑，或者放入死信队列
        }
    }
}
