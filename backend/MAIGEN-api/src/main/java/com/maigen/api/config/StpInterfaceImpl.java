package com.maigen.api.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maigen.api.entity.Permission;
import com.maigen.api.entity.Role;
import com.maigen.api.entity.UserRole;
import com.maigen.api.entity.RolePermission;
import com.maigen.api.service.PermissionService;
import com.maigen.api.service.RoleService;
import com.maigen.api.service.UserRoleService;
import com.maigen.api.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限委派实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<Long> roleIds = getRoleIdList(Long.valueOf(loginId.toString()));
        if (roleIds.isEmpty()) return new ArrayList<>();

        List<Long> permissionIds = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getRoleId, roleIds))
                .stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        
        if (permissionIds.isEmpty()) return new ArrayList<>();

        return permissionService.listByIds(permissionIds)
                .stream().map(Permission::getCode).collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Long> roleIds = getRoleIdList(Long.valueOf(loginId.toString()));
        if (roleIds.isEmpty()) return new ArrayList<>();

        return roleService.listByIds(roleIds)
                .stream().map(Role::getName).collect(Collectors.toList());
    }

    private List<Long> getRoleIdList(Long userId) {
        return userRoleService.list(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }
}
