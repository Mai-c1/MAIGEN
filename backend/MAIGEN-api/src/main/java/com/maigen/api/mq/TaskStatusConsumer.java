package com.maigen.api.mq;

import cn.hutool.json.JSONUtil;
import com.maigen.api.entity.Task;
import com.maigen.api.service.TaskService;
import com.maigen.common.core.enums.TaskStatusEnum;
import com.maigen.common.core.model.dto.TaskResultDTO;
import com.maigen.common.core.model.dto.TaskStatusDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskStatusConsumer {

    private final StringRedisTemplate redisTemplate;
    private final TaskService taskService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_TASK_STATUS)
    public void receiveTaskStatus(TaskStatusDTO statusDTO) {
        log.info("接收到任务状态更新消息: {}", JSONUtil.toJsonStr(statusDTO));
        try {
            // 如果是失败状态，统一走 handleTaskResult 逻辑（包含原子状态更新和积分退还）
            if (TaskStatusEnum.FAILED.getCode().equals(statusDTO.getStatus())) {
                TaskResultDTO resultDTO = new TaskResultDTO();
                resultDTO.setTaskId(statusDTO.getTaskId());
                resultDTO.setSuccess(false);
                resultDTO.setErrorMessage(statusDTO.getMessage());
                taskService.handleTaskResult(resultDTO);
                return;
            }

            // 1. 优先更新数据库，确保列表渲染正确
            taskService.lambdaUpdate()
                    .set(Task::getStatus, statusDTO.getStatus())
                    .set(Task::getProgress, statusDTO.getProgress())
                    .set(Task::getUpdatedAt, LocalDateTime.now())
                    .eq(Task::getId, statusDTO.getTaskId())
                    .update();

            // 2. 更新 Redis 缓存，支撑轮询
            String key = RedisConstants.getTaskProgressKey(statusDTO.getTaskId());
            redisTemplate.opsForValue().set(key, String.valueOf(statusDTO.getProgress()), 1, TimeUnit.HOURS);
            
            log.info("数据库任务状态更新成功: taskId={}, status={}", statusDTO.getTaskId(), statusDTO.getStatus());
        } catch (Exception e) {
            log.error("处理任务状态消息失败", e);
        }
    }
}
