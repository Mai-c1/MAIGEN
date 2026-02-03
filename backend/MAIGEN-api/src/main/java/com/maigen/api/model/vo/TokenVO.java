package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Schema(description = "Token响应信息")
public class TokenVO implements Serializable {

    @Schema(description = "Token名称", example = "satoken")
    private String tokenName;

    @Schema(description = "Token值", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String tokenValue;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户昵称", example = "User_123456")
    private String nickname;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;
}
