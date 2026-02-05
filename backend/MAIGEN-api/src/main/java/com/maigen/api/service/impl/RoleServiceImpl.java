package com.maigen.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Role;
import com.maigen.api.entity.RolePermission;
import com.maigen.api.entity.UserRole;
import com.maigen.api.mapper.RoleMapper;
import com.maigen.api.mapper.UserRoleMapper;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.RoleCreateDTO;
import com.maigen.api.model.dto.admin.RoleQueryDTO;
import com.maigen.api.model.dto.admin.RoleUpdateDTO;
import com.maigen.api.model.vo.RoleVO;
import com.maigen.api.service.RolePermissionService;
import com.maigen.api.service.RoleService;
import com.maigen.common.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 25128
* @description 针对表【role】的数据库操作Service实现
* @createDate 2026-01-29 19:57:59
*/
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionService rolePermissionService;
    private final UserRoleMapper userRoleMapper;

    @Override
    public PageDTO<RoleVO> getRolePage(RoleQueryDTO query) {
        Page<Role> page = query.toMpPage();
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(Role::getName, query.getKeyword())
                   .or()
                   .like(Role::getDescription, query.getKeyword());
        }
        
        wrapper.orderByDesc(Role::getCreatedAt);
        
        this.page(page, wrapper);
        
        // 批量查询权限
        List<Long> roleIds = page.getRecords().stream().map(Role::getId).collect(Collectors.toList());
        Map<Long, List<Long>> rolePermissionMap;
        
        if (!roleIds.isEmpty()) {
            List<RolePermission> rps = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>()
                    .in(RolePermission::getRoleId, roleIds));
            rolePermissionMap = rps.stream()
                    .collect(Collectors.groupingBy(
                            RolePermission::getRoleId,
                            Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())
                    ));
        } else {
            rolePermissionMap = Collections.emptyMap();
        }
        
        return PageDTO.of(page, role -> {
            RoleVO vo = new RoleVO();
            BeanUtil.copyProperties(role, vo);
            vo.setPermissionIds(rolePermissionMap.getOrDefault(role.getId(), Collections.emptyList()));
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleCreateDTO dto) {
        // 1. 检查重名
        if (lambdaQuery().eq(Role::getName, dto.getName()).exists()) {
            throw new CustomException("角色名称已存在", 400);
        }

        // 2. 保存角色
        Role role = new Role();
        BeanUtil.copyProperties(dto, role);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        this.save(role);

        // 3. 保存权限关联
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            List<RolePermission> rps = dto.getPermissionIds().stream().map(pid -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(pid);
                return rp;
            }).collect(Collectors.toList());
            rolePermissionService.saveBatch(rps);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO dto) {
        Role role = this.getById(dto.getId());
        if (role == null) {
            throw new CustomException("角色不存在", 404);
        }

        // 1. 检查重名 (排除自己)
        if (StrUtil.isNotBlank(dto.getName()) && !dto.getName().equals(role.getName())) {
            if (lambdaQuery().eq(Role::getName, dto.getName()).ne(Role::getId, dto.getId()).exists()) {
                throw new CustomException("角色名称已存在", 400);
            }
            role.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        role.setUpdatedAt(LocalDateTime.now());
        this.updateById(role);

        // 2. 更新权限关联 (先删后加)
        if (dto.getPermissionIds() != null) {
            rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, role.getId()));
            
            if (!dto.getPermissionIds().isEmpty()) {
                List<RolePermission> rps = dto.getPermissionIds().stream().map(pid -> {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(role.getId());
                    rp.setPermissionId(pid);
                    return rp;
                }).collect(Collectors.toList());
                rolePermissionService.saveBatch(rps);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 1. 检查是否有用户关联
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (count > 0) {
            throw new CustomException("该角色下仍有用户，无法删除", 400);
        }

        // 2. 删除权限关联
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));

        // 3. 删除角色
        this.removeById(id);
    }
}




