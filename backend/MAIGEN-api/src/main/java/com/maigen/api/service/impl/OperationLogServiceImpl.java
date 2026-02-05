package com.maigen.api.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.OperationLog;
import com.maigen.api.mapper.OperationLogMapper;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.LogQueryDTO;
import com.maigen.api.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public PageDTO<OperationLog> getLogPage(LogQueryDTO query) {
        Page<OperationLog> page = query.toMpPage();
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(query.getModule())) {
            wrapper.eq(OperationLog::getModule, query.getModule());
        }
        if (query.getUserId() != null) {
            wrapper.eq(OperationLog::getUserId, query.getUserId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OperationLog::getStatus, query.getStatus());
        }
        
        // Date range filtering can be added here if needed
        
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        this.page(page, wrapper);
        return PageDTO.of(page, log -> log);
    }
}
