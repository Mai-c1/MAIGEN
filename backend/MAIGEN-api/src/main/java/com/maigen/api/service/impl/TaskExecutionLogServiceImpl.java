package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.TaskExecutionLog;
import com.maigen.api.mapper.TaskExecutionLogMapper;
import com.maigen.api.service.TaskExecutionLogService;
import org.springframework.stereotype.Service;

@Service
public class TaskExecutionLogServiceImpl extends ServiceImpl<TaskExecutionLogMapper, TaskExecutionLog> implements TaskExecutionLogService {
}
