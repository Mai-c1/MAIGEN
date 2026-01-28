package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Role;
import com.maigen.api.service.RoleService;
import com.maigen.api.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【role】的数据库操作Service实现
* @createDate 2026-01-28 19:33:30
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




