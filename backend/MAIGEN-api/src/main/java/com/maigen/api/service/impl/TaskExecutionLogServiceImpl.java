package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.TaskExecutionLog;
import com.maigen.api.service.TaskExecutionLogService;
import com.maigen.api.mapper.TaskExecutionLogMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【task_execution_log】的数据库操作Service实现
* @createDate 2026-01-28 19:33:30
*/
@Service
public class TaskExecutionLogServiceImpl extends ServiceImpl<TaskExecutionLogMapper, TaskExecutionLog>
    implements TaskExecutionLogService{

}




