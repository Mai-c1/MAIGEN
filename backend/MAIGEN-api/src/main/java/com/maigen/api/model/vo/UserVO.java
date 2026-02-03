package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息VO")
public class UserVO implements Serializable {

    @Schema(description = "用户ID", example = "1001")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "昵称", example = "AdminUser")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "个人简介", example = "算法竞赛爱好者")
    private String bio;

    @Schema(description = "邀请码", example = "ABCDEF")
    private String invitationCode;

    @Schema(description = "积分余额", example = "100")
    private Integer points;

    @Schema(description = "账号状态 (1-正常, 0-禁用)", example = "1")
    private Integer status;

    @Schema(description = "注册时间", example = "2023-01-01 00:00:00")
    private LocalDateTime createdAt;
}
