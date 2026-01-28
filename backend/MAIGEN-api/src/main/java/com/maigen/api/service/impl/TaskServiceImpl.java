package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Task;
import com.maigen.api.service.TaskService;
import com.maigen.api.mapper.TaskMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【task】的数据库操作Service实现
* @createDate 2026-01-28 19:33:30
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService{

}




