package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.admin.PermissionCreateDTO;
import com.maigen.api.model.dto.admin.PermissionQueryDTO;
import com.maigen.api.model.dto.admin.PermissionUpdateDTO;
import com.maigen.api.model.vo.PermissionVO;
import com.maigen.api.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/permission")
@RequiredArgsConstructor
@Tag(name = "管理端-权限管理")
public class ManagePermissionController {

    private final PermissionService permissionService;

    @GetMapping("/list")
    @Operation(summary = "权限列表(分页)")
    @SaCheckPermission("permission:view")
    public SaResult list(PermissionQueryDTO query) {
        PageDTO<PermissionVO> page = permissionService.getPermissionPage(query);
        return SaResult.data(page);
    }
    
    @GetMapping("/list-all")
    @Operation(summary = "所有权限(用于选择)")
    @SaCheckPermission("permission:view")
    public SaResult listAll() {
        List<PermissionVO> list = permissionService.getAllPermissions();
        return SaResult.data(list);
    }

    @PostMapping("/create")
    @Operation(summary = "新增权限")
    @SaCheckPermission("permission:add")
    public SaResult create(@Validated @RequestBody PermissionCreateDTO dto) {
        permissionService.createPermission(dto);
        return SaResult.ok("创建成功");
    }

    @PostMapping("/update")
    @Operation(summary = "更新权限")
    @SaCheckPermission("permission:edit")
    public SaResult update(@Validated @RequestBody PermissionUpdateDTO dto) {
        permissionService.updatePermission(dto);
        return SaResult.ok("更新成功");
    }

    @PostMapping("/delete")
    @Operation(summary = "删除权限")
    @SaCheckPermission("permission:delete")
    public SaResult delete(@RequestParam Long id) {
        permissionService.deletePermission(id);
        return SaResult.ok("删除成功");
    }
}
