package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.RoleCreateDTO;
import com.maigen.api.model.dto.admin.RoleQueryDTO;
import com.maigen.api.model.dto.admin.RoleUpdateDTO;
import com.maigen.api.model.vo.RoleVO;
import com.maigen.api.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/role")
@RequiredArgsConstructor
@Tag(name = "管理端-角色管理")
public class ManageRoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @Operation(summary = "角色列表")
    @SaCheckPermission("role:view")
    public SaResult list(RoleQueryDTO query) {
        PageDTO<RoleVO> page = roleService.getRolePage(query);
        return SaResult.data(page);
    }

    @PostMapping("/create")
    @Operation(summary = "新增角色")
    @SaCheckPermission("role:add")
    public SaResult create(@Validated @RequestBody RoleCreateDTO dto) {
        roleService.createRole(dto);
        return SaResult.ok("创建成功");
    }

    @PostMapping("/update")
    @Operation(summary = "更新角色")
    @SaCheckPermission("role:edit")
    public SaResult update(@Validated @RequestBody RoleUpdateDTO dto) {
        roleService.updateRole(dto);
        return SaResult.ok("更新成功");
    }

    @PostMapping("/delete")
    @Operation(summary = "删除角色")
    @SaCheckPermission("role:delete")
    public SaResult delete(@RequestParam Long id) {
        roleService.deleteRole(id);
        return SaResult.ok("删除成功");
    }
}
