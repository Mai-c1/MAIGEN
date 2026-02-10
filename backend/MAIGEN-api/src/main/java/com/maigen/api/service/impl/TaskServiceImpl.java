package com.maigen.api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.AiWorkflow;
import com.maigen.api.entity.GeneratedData;
import com.maigen.api.entity.Task;
import com.maigen.api.entity.User;
import com.maigen.api.mapper.TaskMapper;
import com.maigen.api.mapper.UserMapper;
import com.maigen.api.model.dto.CreateTaskDTO;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.model.vo.TaskDetailVO;
import com.maigen.api.model.vo.TaskStatisticsVO;
import com.maigen.api.model.vo.TaskStatusVO;
import com.maigen.api.service.*;
import com.maigen.common.core.constant.PointsConstants;
import com.maigen.common.core.enums.TaskStatusEnum;
import com.maigen.common.core.exception.CustomException;
import com.maigen.common.core.model.dto.TaskResultDTO;
import com.maigen.common.core.model.dto.TaskSubmitDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 25128
 * @description 针对表【task】的数据库操作Service实现
 * @createDate 2026-01-29 19:57:59
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
        implements TaskService {

    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final PointsRecordService pointsRecordService;
    private final GeneratedDataService generatedDataService;
    private final AiWorkflowService workflowService;
    private final TaskExecutionLogService taskExecutionLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(CreateTaskDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 扣减积分
        User user = userMapper.selectById(userId);
        if (user.getPoints() < PointsConstants.GENERATE_TASK_COST) {
            throw new CustomException("积分不足", 400001);
        }
        user.setPoints(user.getPoints() - PointsConstants.GENERATE_TASK_COST);
        userMapper.updateById(user);

        // 2. 记录积分流水
        com.maigen.api.entity.PointsRecord record = com.maigen.api.entity.PointsRecord.builder()
                .userId(userId)
                .amount(-PointsConstants.GENERATE_TASK_COST)
                .source(PointsConstants.SOURCE_TASK_CREATE)
                .description("创建任务: " + dto.getTitle())
                .createdAt(LocalDateTime.now())
                .build();
        
        // 3. 创建任务记录
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(dto.getTitle());
        task.setProblemDescription(dto.getProblemDescription());
        task.setStandardCode(dto.getStandardCode());
        task.setTimeLimit(dto.getTimeLimit());
        task.setMemoryLimit(dto.getMemoryLimit());
        task.setTestcaseCount(dto.getTestcaseCount());
        task.setWorkflowId(dto.getWorkflowId());
        
        task.setStatus(TaskStatusEnum.PENDING.getCode()); // 待处理
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        this.save(task);

        // 记录关联ID并保存流水
        record.setRelatedId(task.getId().toString());
        pointsRecordService.save(record);

        // 4. 将任务详细数据存入 Redis (Claim Check Pattern)
        TaskSubmitDTO submitDTO = new TaskSubmitDTO();
        submitDTO.setTaskId(task.getId());
        submitDTO.setUserId(userId);
        submitDTO.setTitle(dto.getTitle());
        submitDTO.setProblemDescription(dto.getProblemDescription());
        submitDTO.setStandardCode(dto.getStandardCode());
        submitDTO.setTimeLimit(dto.getTimeLimit());
        submitDTO.setMemoryLimit(dto.getMemoryLimit());
        submitDTO.setTestcaseCount(dto.getTestcaseCount());
        submitDTO.setWorkflowId(dto.getWorkflowId());

        String dataKey = RedisConstants.getTaskDataKey(task.getId());
        redisTemplate.opsForValue().set(dataKey, JSONUtil.toJsonStr(submitDTO), 7, TimeUnit.DAYS);

        // 5. 发送轻量级 MQ 消息给 Analysis 模块 (仅传递 taskId)
        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, RabbitMQConstants.ROUTING_TASK_SUBMIT, task.getId());

        // 6. 初始化 Redis 进度缓存
        String progressKey = RedisConstants.getTaskProgressKey(task.getId());
        redisTemplate.opsForValue().set(progressKey, "0", 1, TimeUnit.HOURS); // 初始状态 0

        return task.getId();
    }

    @Override
    public TaskStatusVO getTaskStatus(Long taskId) {
        // 1. 优先查 Redis 获取进度
        String progressKey = RedisConstants.getTaskProgressKey(taskId);
        String progressStr = redisTemplate.opsForValue().get(progressKey);
        Integer progress = StrUtil.isNotBlank(progressStr) ? Integer.parseInt(progressStr) : null;

        // 2. 查 DB 获取基本信息
        Task task = getById(taskId);
        if (task == null) {
            throw new CustomException("任务不存在", 500001);
        }

        // 校验权限
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getUserId().equals(userId)) {
            throw new CustomException("无权访问该任务", 500002);
        }

        TaskStatusVO vo = TaskStatusVO.builder()
                .taskId(taskId)
                .title(task.getTitle())
                .status(task.getStatus())
                .progress(progress != null ? progress : task.getProgress())
                .errorMessage(task.getErrorMessage())
                .createTime(task.getCreatedAt())
                .updateTime(task.getUpdatedAt())
                .build();

        // 如果已完成，获取下载链接
        if (TaskStatusEnum.COMPLETED.getCode().equals(task.getStatus())) {
            GeneratedData data = generatedDataService.getOne(new LambdaQueryWrapper<GeneratedData>()
                    .eq(GeneratedData::getTaskId, taskId)
                    .orderByDesc(GeneratedData::getCreatedAt)
                    .last("limit 1"));
            if (data != null) {
                vo.setResultUrl(data.getDownloadUrl());
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTaskResult(TaskResultDTO resultDTO) {
        Long taskId = resultDTO.getTaskId();
        
        // 1. 原子更新：只有在非终态的情况下才进行状态更新
        // 使用 LambdaUpdate 确保 SQL 级别原子性：update task set status=?,... where id=? and status not in (4,5,6,7)
        boolean updated = this.lambdaUpdate()
                .set(Task::getStatus, resultDTO.isSuccess() ? TaskStatusEnum.COMPLETED.getCode() : TaskStatusEnum.FAILED.getCode())
                .set(Task::getProgress, resultDTO.isSuccess() ? 100 : 0)
                .set(Task::getErrorMessage, resultDTO.isSuccess() ? null : resultDTO.getErrorMessage())
                .set(Task::getUpdatedAt, LocalDateTime.now())
                .eq(Task::getId, taskId)
                .notIn(Task::getStatus, Arrays.asList(
                        TaskStatusEnum.COMPLETED.getCode(),
                        TaskStatusEnum.FAILED.getCode(),
                        TaskStatusEnum.TIMEOUT.getCode(),
                        TaskStatusEnum.CANCELLED.getCode()
                ))
                .update();

        // 如果更新行数为 0，说明任务不存在或已经处理过（幂等返回）
        if (!updated) {
            log.info("任务结果处理跳过（任务不存在或已是终态）: taskId={}", taskId);
            return;
        }

        // 2. 状态更新成功后，执行后续逻辑
        Task task = this.getById(taskId); // 此时查出的状态肯定是最新的且由当前线程更新

        if (resultDTO.isSuccess()) {
            // 保存生成数据
            GeneratedData generatedData = new GeneratedData();
            generatedData.setTaskId(task.getId());
            generatedData.setFileName("result_" + task.getId() + ".zip");
            // 存储简短的 MinIO 路径：bucket/object
            String shortPath = resultDTO.getBucketName() + "/" + resultDTO.getObjectName();
            generatedData.setFilePath(shortPath);
            generatedData.setDownloadUrl(resultDTO.getDownloadUrl());
            generatedData.setStatus(1);
            generatedData.setCreatedAt(LocalDateTime.now());
            generatedDataService.save(generatedData);
        } else {
            // 失败退还积分
            Long userId = task.getUserId();
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setPoints(user.getPoints() + PointsConstants.GENERATE_TASK_COST);
                userMapper.updateById(user);
                
                // 记录退还流水
                pointsRecordService.save(com.maigen.api.entity.PointsRecord.builder()
                        .userId(userId)
                        .amount(PointsConstants.GENERATE_TASK_COST)
                        .source(PointsConstants.SOURCE_TASK_REFUND)
                        .relatedId(task.getId().toString())
                        .description("任务生成失败退还积分: " + task.getTitle())
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        // 3. 同步更新 Redis 缓存 (存储进度值 0-100)
        String key = RedisConstants.getTaskProgressKey(task.getId());
        Integer progress = TaskStatusEnum.COMPLETED.getCode().equals(task.getStatus()) ? 100 : 0;
        redisTemplate.opsForValue().set(key, String.valueOf(progress), 1, TimeUnit.HOURS);
    }

    @Override
    public PageDTO<TaskStatusVO> getTaskList(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Task> page = query.toMpPage();

        lambdaQuery()
                .eq(Task::getUserId, userId)
                .orderByDesc(Task::getCreatedAt)
                .page(page);

        return PageDTO.of(page, task -> TaskStatusVO.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .progress(task.getProgress())
                .errorMessage(task.getErrorMessage())
                .createTime(task.getCreatedAt())
                .updateTime(task.getUpdatedAt())
                .build());
    }

    @Override
    public TaskDetailVO getTaskDetail(Long taskId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new CustomException("任务不存在", 500001);
        }

        // 校验权限
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getUserId().equals(userId)) {
            throw new CustomException("无权访问该任务", 500002);
        }

        // 获取当前进度
        String progressKey = RedisConstants.getTaskProgressKey(taskId);
        String progressStr = redisTemplate.opsForValue().get(progressKey);
        Integer progress = StrUtil.isNotBlank(progressStr) ? Integer.parseInt(progressStr) : task.getProgress();

        int status = task.getStatus();

        // 获取方案信息
        String workflowName = "Unknown";
        String workflowDescription = "";
        if (task.getWorkflowId() != null) {
            AiWorkflow workflow = workflowService.getById(task.getWorkflowId());
            if (workflow != null) {
                workflowName = workflow.getName();
                workflowDescription = workflow.getDescription();
            }
        }

        TaskDetailVO vo = TaskDetailVO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .problemDescription(task.getProblemDescription())
                .standardCode(task.getStandardCode())
                .testcaseCount(task.getTestcaseCount())
                .timeLimit(task.getTimeLimit())
                .memoryLimit(task.getMemoryLimit())
                .workflowId(task.getWorkflowId())
                .workflowName(workflowName)
                .workflowDescription(workflowDescription)
                .status(status)
                .statusDesc(TaskStatusEnum.getByCode(status).getDesc())
                .progress(progress)
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();

        // 获取结果 URL
        if (TaskStatusEnum.COMPLETED.getCode().equals(task.getStatus())) {
            GeneratedData data = generatedDataService.getOne(new LambdaQueryWrapper<GeneratedData>()
                    .eq(GeneratedData::getTaskId, taskId)
                    .orderByDesc(GeneratedData::getCreatedAt)
                    .last("limit 1"));
            if (data != null) {
                vo.setResultUrl(data.getDownloadUrl());
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId) {
        Task task = getById(taskId);
        if (task == null) throw new CustomException("任务不存在", 500001);
        if (!task.getUserId().equals(StpUtil.getLoginIdAsLong())) throw new CustomException("无权操作", 500002);

        // 只有未完成的状态可以取消
        if (TaskStatusEnum.getByCode(task.getStatus()).isFinal()) {
            throw new CustomException("任务已结束，无法取消", 500004);
        }

        task.setStatus(TaskStatusEnum.CANCELLED.getCode());
        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);

        // 退还积分
        User user = userMapper.selectById(task.getUserId());
        user.setPoints(user.getPoints() + PointsConstants.GENERATE_TASK_COST);
        userMapper.updateById(user);

        // 记录退还流水
        pointsRecordService.save(com.maigen.api.entity.PointsRecord.builder()
                .userId(task.getUserId())
                .amount(PointsConstants.GENERATE_TASK_COST)
                .source(PointsConstants.SOURCE_TASK_REFUND)
                .relatedId(task.getId().toString())
                .description("用户取消任务退还积分: " + task.getTitle())
                .createdAt(LocalDateTime.now())
                .build());

        // 清理 Redis
        redisTemplate.delete(RedisConstants.getTaskProgressKey(taskId));
        redisTemplate.delete(RedisConstants.getTaskDataKey(taskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        Task task = getById(taskId);
        if (task == null) return;
        if (!task.getUserId().equals(StpUtil.getLoginIdAsLong())) throw new CustomException("无权操作", 500002);

        // 1. 删除关联数据
        generatedDataService.remove(new LambdaQueryWrapper<GeneratedData>().eq(GeneratedData::getTaskId, taskId));
        
        // 2. 删除任务本身
        removeById(taskId);

        // 3. 清理 Redis
        redisTemplate.delete(RedisConstants.getTaskProgressKey(taskId));
        redisTemplate.delete(RedisConstants.getTaskDataKey(taskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryTask(Long taskId) {
        Task task = this.getById(taskId);
        if (task == null) {
            throw new CustomException("任务不存在", 500001);
        }

        // 校验权限
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getUserId().equals(userId)) {
            throw new CustomException("无权操作该任务", 500002);
        }

        // 允许任何终态或非进行中状态的任务重试（包括已完成、失败、取消等）
        // 只要不是正在进行中，就可以重试
        if (!TaskStatusEnum.getByCode(task.getStatus()).isFinal()) {
            throw new CustomException("任务正在进行中，请等待执行完成", 500003);
        }

        // 1. 更新任务状态
        task.setStatus(TaskStatusEnum.PENDING.getCode());
        task.setProgress(0);
        task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
        task.setErrorMessage(null);
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);

        // 2. 清理之前的日志（可选，为了让控制台看起来像新的一样）
        taskExecutionLogService.remove(new LambdaQueryWrapper<com.maigen.api.entity.TaskExecutionLog>()
                .eq(com.maigen.api.entity.TaskExecutionLog::getTaskId, taskId));

        // 3. 将任务详细数据存入 Redis (Claim Check Pattern)
        // 重新构建 submitDTO，确保 workflowId 存在
        TaskSubmitDTO submitDTO = new TaskSubmitDTO();
        submitDTO.setTaskId(task.getId());
        submitDTO.setUserId(userId);
        submitDTO.setTitle(task.getTitle());
        submitDTO.setProblemDescription(task.getProblemDescription());
        submitDTO.setStandardCode(task.getStandardCode());
        submitDTO.setTimeLimit(task.getTimeLimit());
        submitDTO.setMemoryLimit(task.getMemoryLimit());
        submitDTO.setTestcaseCount(task.getTestcaseCount());
        submitDTO.setWorkflowId(task.getWorkflowId());

        String dataKey = RedisConstants.getTaskDataKey(task.getId());
        redisTemplate.opsForValue().set(dataKey, JSONUtil.toJsonStr(submitDTO), 1, TimeUnit.HOURS);
        
        // 重置进度缓存
        redisTemplate.opsForValue().set(RedisConstants.getTaskProgressKey(taskId), "0", 1, TimeUnit.HOURS);

        // 4. 发送 MQ (轻量级)
        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, RabbitMQConstants.ROUTING_TASK_SUBMIT, task.getId());
    }

    @Override
    public TaskStatisticsVO getTaskStatistics() {
        Long userId = StpUtil.getLoginIdAsLong();

        // 统计各状态数量
        // ... (existing code)
        Long inProgressCount = this.lambdaQuery()
                .eq(Task::getUserId, userId)
                .in(Task::getStatus, Arrays.asList(
                        TaskStatusEnum.PENDING.getCode(),
                        TaskStatusEnum.ANALYZING.getCode(),
                        TaskStatusEnum.GENERATING.getCode(),
                        TaskStatusEnum.VERIFYING.getCode()
                ))
                .count();

        Long completedCount = this.lambdaQuery()
                .eq(Task::getUserId, userId)
                .eq(Task::getStatus, TaskStatusEnum.COMPLETED.getCode())
                .count();

        Long failedCount = this.lambdaQuery()
                .eq(Task::getUserId, userId)
                .in(Task::getStatus, Arrays.asList(
                        TaskStatusEnum.FAILED.getCode(),
                        TaskStatusEnum.TIMEOUT.getCode()
                ))
                .count();

        Long totalCount = this.lambdaQuery()
                .eq(Task::getUserId, userId)
                .count();

        return TaskStatisticsVO.builder()
                .inProgressCount(inProgressCount)
                .completedCount(completedCount)
                .failedCount(failedCount)
                .totalCount(totalCount)
                .build();
    }
    
    @Override
    public List<com.maigen.api.entity.TaskExecutionLog> getTaskLogs(Long taskId) {
        // 1. 校验任务是否存在及权限
        Task task = getById(taskId);
        if (task == null) throw new CustomException("任务不存在", 500001);
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getUserId().equals(userId)) throw new CustomException("无权访问该任务", 500002);
        
        // 2. 查询日志
        return taskExecutionLogService.lambdaQuery()
                .eq(com.maigen.api.entity.TaskExecutionLog::getTaskId, taskId)
                .orderByAsc(com.maigen.api.entity.TaskExecutionLog::getId)
                .list();
    }
}
