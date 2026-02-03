package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maigen.api.entity.User;
import com.maigen.api.entity.UserRole;
import com.maigen.api.entity.Role;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.model.dto.admin.AdminUserDTO;
import com.maigen.api.model.dto.admin.PointsAdjustmentDTO;
import com.maigen.api.service.*;
import com.maigen.common.core.annotation.Log;
import com.maigen.common.core.constant.PointsConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.secure.SaSecureUtil;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "管理后台-用户管理", description = "用户全生命周期管理、积分调账及角色分配")
@SaCheckRole("管理员")
public class ManageUserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final PointsRecordService pointsRecordService;
    private final InvitationService invitationService;

    @GetMapping("/list")
    @Operation(summary = "用户列表")
    @SaCheckPermission("user:view")
    public SaResult listUsers(PageQuery query, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword).or().like(User::getEmail, keyword);
        }
        return SaResult.data(userService.page(query.toMpPage(), wrapper));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @Log(module = "用户管理", operation = "新增用户")
    @SaCheckPermission("user:create")
    public SaResult createUser(@RequestBody AdminUserDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            return SaResult.error("用户名和密码不能为空");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(SaSecureUtil.md5BySalt(dto.getPassword(), "MAIGEN_SALT"));
        user.setStatus(1);
        userService.save(user);

        // 分配角色
        if (StringUtils.hasText(dto.getRole())) {
            assignRole(user.getId(), dto.getRole());
        }
        return SaResult.ok("创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    @Log(module = "用户管理", operation = "更新用户信息")
    @SaCheckPermission("user:edit")
    public SaResult updateUser(@PathVariable Long id, @RequestBody AdminUserDTO dto) {
        User user = userService.getById(id);
        if (user == null) return SaResult.error("用户不存在");
        
        if (StringUtils.hasText(dto.getEmail())) user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) user.setPassword(SaSecureUtil.md5BySalt(dto.getPassword(), "MAIGEN_SALT"));
        
        userService.updateById(user);

        if (StringUtils.hasText(dto.getRole())) {
            assignRole(user.getId(), dto.getRole());
        }
        return SaResult.ok("更新成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @Log(module = "用户管理", operation = "修改用户状态")
    @SaCheckPermission("user:status:update")
    public SaResult updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) return SaResult.error("用户不存在");
        user.setStatus(status);
        userService.updateById(user);
        return SaResult.ok("状态更新成功");
    }

    @PostMapping("/{id}/points")
    @Operation(summary = "积分调账")
    @Log(module = "用户管理", operation = "积分调账")
    @SaCheckPermission("user:points:edit")
    public SaResult adjustPoints(@PathVariable Long id, @RequestBody PointsAdjustmentDTO dto) {
        User user = userService.getById(id);
        if (user == null) return SaResult.error("用户不存在");
        
        // 使用 PointsRecordService 的逻辑来更新积分并记录流水
        pointsRecordService.rewardPoints(id, dto.getAmount(), PointsConstants.SOURCE_ADMIN_ADJUST, "管理员手动调账: " + dto.getReason());
        return SaResult.ok("积分调整成功");
    }

    @GetMapping("/invitations")
    @Operation(summary = "邀请记录查询")
    @SaCheckPermission("user:view")
    public SaResult listInvitations(PageQuery query) {
        return SaResult.data(invitationService.page(query.toMpPage()));
    }

    private void assignRole(Long userId, String roleName) {
        // 映射前端传来的角色标识到数据库中的角色名称
        String actualRoleName = "admin".equalsIgnoreCase(roleName) ? "管理员" : "普通用户";
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getName, actualRoleName));
        if (role != null) {
            userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            userRoleService.save(ur);
        }
    }
}
