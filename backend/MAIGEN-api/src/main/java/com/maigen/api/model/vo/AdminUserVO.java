package com.maigen.api.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private Integer points;
    private Integer status;
    private String role; // 角色标识 (admin/user)
    private LocalDateTime createdAt;
}
