package com.maigen.api.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员用户操作 DTO")
public class AdminUserDTO {
    @Schema(description = "用户 ID (更新时必填)")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "密码 (新增时必填)")
    private String password;

    @Schema(description = "角色 (admin/user)")
    private String role;

    @Schema(description = "状态 (1-正常, 0-禁用)")
    private Integer status;
}
