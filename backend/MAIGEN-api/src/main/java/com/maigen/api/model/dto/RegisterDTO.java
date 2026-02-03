package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO implements Serializable {

    @Schema(description = "用户名", example = "mai")
    private String username;

    @Schema(description = "邮箱地址", example = "test@example.com")
    private String email;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "验证码", example = "123456")
    private String code; // 验证码

    @Schema(description = "邀请码 (可选)", example = "ABCDEF")
    private String invitationCode; // 邀请码 (可选)
}
