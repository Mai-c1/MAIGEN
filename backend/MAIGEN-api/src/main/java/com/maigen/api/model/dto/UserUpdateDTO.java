package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户更新请求参数")
public class UserUpdateDTO {

    @Schema(description = "新昵称", example = "NewName")
    private String nickname;

    @Schema(description = "新头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "个人简介", example = "算法竞赛爱好者")
    private String bio;
}
