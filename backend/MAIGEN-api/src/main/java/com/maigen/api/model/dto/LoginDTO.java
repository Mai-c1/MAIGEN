package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户登录请求参数")
public class LoginDTO implements Serializable {

    @Schema(description = "用户名或邮箱", example = "admin")
    private String username; // 支持用户名或邮箱

    @Schema(description = "密码", example = "123456")
    private String password;
}
