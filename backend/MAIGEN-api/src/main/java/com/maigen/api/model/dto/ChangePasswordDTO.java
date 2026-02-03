package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "修改密码请求参数")
public class ChangePasswordDTO implements Serializable {

    @Schema(description = "旧密码", example = "123456")
    private String oldPassword;

    @Schema(description = "新密码", example = "654321")
    private String newPassword;
}
