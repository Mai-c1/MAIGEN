package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "忘记密码请求参数")
public class ForgetPasswordDTO implements Serializable {

    @Schema(description = "邮箱地址", example = "test@example.com")
    private String email;

    @Schema(description = "验证码", example = "123456")
    private String code;

    @Schema(description = "新密码", example = "newPassword123")
    private String newPassword;
}
