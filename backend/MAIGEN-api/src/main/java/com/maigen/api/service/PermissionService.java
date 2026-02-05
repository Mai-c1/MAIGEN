package com.maigen.api.service;

import com.maigen.api.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.PermissionCreateDTO;
import com.maigen.api.model.dto.admin.PermissionQueryDTO;
import com.maigen.api.model.dto.admin.PermissionUpdateDTO;
import com.maigen.api.model.vo.PermissionVO;

import java.util.List;

/**
* @author 25128
* @description 针对表【permission】的数据库操作Service
* @createDate 2026-01-29 19:57:59
*/
public interface PermissionService extends IService<Permission> {

    PageDTO<PermissionVO> getPermissionPage(PermissionQueryDTO query);
    
    List<PermissionVO> getAllPermissions(); // For Role selection

    void createPermission(PermissionCreateDTO dto);

    void updatePermission(PermissionUpdateDTO dto);

    void deletePermission(Long id);
}
