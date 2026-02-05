package com.maigen.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maigen.api.entity.OperationLog;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.LogQueryDTO;

public interface OperationLogService extends IService<OperationLog> {
    PageDTO<OperationLog> getLogPage(LogQueryDTO query);
}
