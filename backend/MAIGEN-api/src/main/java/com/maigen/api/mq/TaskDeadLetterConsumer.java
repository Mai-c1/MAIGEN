package com.maigen.api.mq;

import cn.hutool.core.util.StrUtil;
import com.maigen.api.entity.Task;
import com.maigen.api.service.TaskService;
import com.maigen.common.core.enums.TaskStatusEnum;
import com.maigen.common.core.model.dto.TaskErrorDTO;
import com.maigen.common.core.model.dto.TaskResultDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskDeadLetterConsumer {

    private final TaskService taskService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 监听任务全链路死信队列
     * 收集 submit, execute, update, status 等所有环节消费失败的消息
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_TASK_DEAD)
    public void receiveDeadLetter(Object message) {
        log.error("收到死信消息，任务处理彻底失败: {}", message);
        
        try {
            Long taskId = null;
            String errorMessage = "系统处理异常：任务处理超时或重试耗尽进入死信队列";

            // 1. 健壮的消息解析逻辑
            if (message instanceof Long) {
                taskId = (Long) message;
            } else if (message instanceof TaskErrorDTO) {
                TaskErrorDTO errorDTO = (TaskErrorDTO) message;
                taskId = errorDTO.getTaskId();
                errorMessage = errorDTO.getErrorMessage();
            } else if (message instanceof TaskResultDTO) {
                TaskResultDTO resultDTO = (TaskResultDTO) message;
                taskId = resultDTO.getTaskId();
                errorMessage = StrUtil.blankToDefault(resultDTO.getErrorMessage(), errorMessage);
            } else if (message instanceof Integer) {
                taskId = ((Integer) message).longValue();
            }

            if (taskId != null) {
                // 2. 第一层：Redis 预检 (性能优化)
                // 检查 Redis 中的进度/状态，如果是终态则直接跳过
                String progressKey = RedisConstants.getTaskProgressKey(taskId);
                String statusStr = redisTemplate.opsForValue().get(progressKey);
                if (StrUtil.isNotBlank(statusStr)) {
                    int status = Integer.parseInt(statusStr);
                    if (TaskStatusEnum.getByCode(status).isFinal()) {
                        log.info("Redis 预检：任务已处于终态，跳过死信处理: taskId={}", taskId);
                        return;
                    }
                }

                // 3. 执行统一的失败处理逻辑 (内部包含第二层：数据库原子更新幂等)
                TaskResultDTO result = new TaskResultDTO();
                result.setTaskId(taskId);
                result.setSuccess(false);
                result.setErrorMessage(errorMessage);
                
                log.info("正在通过死信逻辑关闭任务: taskId={}, error={}", taskId, errorMessage);
                taskService.handleTaskResult(result);
            } else {
                log.warn("无法从死信消息中解析出 taskId: {}", message);
            }
        } catch (Exception e) {
            log.error("处理死信消息时发生异常", e);
        }
    }
}
