package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.OperationLog;
import com.maigen.api.mapper.OperationLogMapper;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public PageDTO<OperationLog> getLogPage(PageQuery query) {
        Page<OperationLog> page = query.toMpPage();
        this.lambdaQuery().orderByDesc(OperationLog::getCreatedAt).page(page);
        return PageDTO.of(page, log -> log);
    }
}
