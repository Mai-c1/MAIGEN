package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Permission;
import com.maigen.api.service.PermissionService;
import com.maigen.api.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【permission】的数据库操作Service实现
* @createDate 2026-01-28 19:33:30
*/
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
    implements PermissionService{

}




