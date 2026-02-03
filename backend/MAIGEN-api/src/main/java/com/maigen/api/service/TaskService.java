package com.maigen.api.service;

import com.maigen.api.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maigen.api.model.dto.CreateTaskDTO;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.model.vo.TaskDetailVO;
import com.maigen.api.model.vo.TaskStatusVO;
import com.maigen.common.core.model.dto.TaskResultDTO;
import com.maigen.api.model.vo.TaskStatisticsVO;
import java.util.List;

/**
 * @author 25128
 * @description 针对表【task】的数据库操作Service
 * @createDate 2026-01-29 19:57:59
 */
public interface TaskService extends IService<Task> {

    Long createTask(CreateTaskDTO dto);

    List<com.maigen.api.entity.TaskStrategy> getStrategies();

    TaskStatusVO getTaskStatus(Long taskId);

    TaskDetailVO getTaskDetail(Long taskId);

    void cancelTask(Long taskId);

    void deleteTask(Long taskId);

    void handleTaskResult(TaskResultDTO resultDTO);

    PageDTO<TaskStatusVO> getTaskList(PageQuery query);

    void retryTask(Long taskId);

    TaskStatisticsVO getTaskStatistics();
}
