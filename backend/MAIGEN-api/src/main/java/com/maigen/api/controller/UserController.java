package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.dto.ChangePasswordDTO;
import com.maigen.api.model.dto.UserUpdateDTO;
import com.maigen.api.service.UserService;
import com.maigen.common.core.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户中心", description = "个人资料管理与安全设置")
@SaCheckLogin
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(summary = "获取个人信息")
    public SaResult getInfo() {
        return SaResult.data(userService.getUserInfo());
    }

    @PutMapping("/info")
    @Operation(summary = "修改个人资料")
    @Log(module = "用户中心", operation = "修改个人资料")
    public SaResult update(@RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return SaResult.ok("修改成功");
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @Log(module = "用户中心", operation = "修改密码")
    public SaResult changePassword(@RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return SaResult.ok("修改成功");
    }
}
