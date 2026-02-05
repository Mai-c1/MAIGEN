package com.maigen.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Permission;
import com.maigen.api.entity.RolePermission;
import com.maigen.api.mapper.PermissionMapper;
import com.maigen.api.mapper.RolePermissionMapper;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.PermissionCreateDTO;
import com.maigen.api.model.dto.admin.PermissionQueryDTO;
import com.maigen.api.model.dto.admin.PermissionUpdateDTO;
import com.maigen.api.model.vo.PermissionVO;
import com.maigen.api.service.PermissionService;
import com.maigen.common.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 25128
* @description 针对表【permission】的数据库操作Service实现
* @createDate 2026-01-29 19:57:59
*/
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public PageDTO<PermissionVO> getPermissionPage(PermissionQueryDTO query) {
        Page<Permission> page = query.toMpPage();
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(Permission::getName, query.getKeyword())
                   .or()
                   .like(Permission::getCode, query.getKeyword())
                   .or()
                   .like(Permission::getDescription, query.getKeyword());
        }
        
        wrapper.orderByDesc(Permission::getCreatedAt);
        this.page(page, wrapper);
        
        return PageDTO.of(page, p -> {
            PermissionVO vo = new PermissionVO();
            BeanUtil.copyProperties(p, vo);
            return vo;
        });
    }

    @Override
    public List<PermissionVO> getAllPermissions() {
        return this.list().stream().map(p -> {
            PermissionVO vo = new PermissionVO();
            BeanUtil.copyProperties(p, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPermission(PermissionCreateDTO dto) {
        if (lambdaQuery().eq(Permission::getCode, dto.getCode()).exists()) {
            throw new CustomException("权限标识已存在", 400);
        }

        Permission permission = new Permission();
        BeanUtil.copyProperties(dto, permission);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        this.save(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(PermissionUpdateDTO dto) {
        Permission permission = this.getById(dto.getId());
        if (permission == null) {
            throw new CustomException("权限不存在", 404);
        }

        if (StrUtil.isNotBlank(dto.getCode()) && !dto.getCode().equals(permission.getCode())) {
            if (lambdaQuery().eq(Permission::getCode, dto.getCode()).ne(Permission::getId, dto.getId()).exists()) {
                throw new CustomException("权限标识已存在", 400);
            }
            permission.setCode(dto.getCode());
        }

        if (dto.getName() != null) permission.setName(dto.getName());
        if (dto.getDescription() != null) permission.setDescription(dto.getDescription());
        permission.setUpdatedAt(LocalDateTime.now());
        
        this.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        // 检查是否有角色使用
        Long count = rolePermissionMapper.selectCount(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, id));
        if (count > 0) {
            throw new CustomException("该权限已被角色引用，无法删除", 400);
        }
        
        this.removeById(id);
    }
}




