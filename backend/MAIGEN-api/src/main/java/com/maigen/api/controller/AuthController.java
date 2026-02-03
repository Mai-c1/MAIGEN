package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.dto.ForgetPasswordDTO;
import com.maigen.api.model.dto.LoginDTO;
import com.maigen.api.model.dto.RegisterDTO;
import com.maigen.api.model.vo.TokenVO;
import com.maigen.api.service.UserService;
import com.maigen.common.core.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@SaIgnore
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录、找回密码及验证码接口")
public class AuthController {

    private final UserService userService;

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "向指定邮箱发送验证码，支持注册(register)和重置密码(reset)两种类型")
    public SaResult sendCode(@RequestParam String email, @RequestParam(defaultValue = "register") String type) {
        userService.sendCode(email, type);
        return SaResult.ok("发送成功");
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用邮箱、密码和验证码注册新用户")
    public SaResult register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return SaResult.ok();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名或邮箱登录，返回Token信息")
    public SaResult login(@RequestBody LoginDTO dto) {
        return SaResult.data(userService.login(dto));
    }

    @PostMapping("/forget-password")
    @Operation(summary = "忘记密码", description = "通过验证码重置用户密码")
    public SaResult forgetPassword(@RequestBody ForgetPasswordDTO dto) {
        userService.forgetPassword(dto);
        return SaResult.ok();
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "注销当前登录状态")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok();
    }
}
