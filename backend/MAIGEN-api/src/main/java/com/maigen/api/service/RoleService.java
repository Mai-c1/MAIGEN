package com.maigen.api.service;

import com.maigen.api.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.RoleCreateDTO;
import com.maigen.api.model.dto.admin.RoleQueryDTO;
import com.maigen.api.model.dto.admin.RoleUpdateDTO;
import com.maigen.api.model.vo.RoleVO;

/**
* @author 25128
* @description 针对表【role】的数据库操作Service
* @createDate 2026-01-29 19:57:59
*/
public interface RoleService extends IService<Role> {

    PageDTO<RoleVO> getRolePage(RoleQueryDTO query);

    void createRole(RoleCreateDTO dto);

    void updateRole(RoleUpdateDTO dto);

    void deleteRole(Long id);
}
