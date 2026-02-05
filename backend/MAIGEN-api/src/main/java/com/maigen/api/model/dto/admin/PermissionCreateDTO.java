package com.maigen.api.model.dto.admin;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PermissionCreateDTO {
    @NotBlank(message = "权限标识不能为空")
    private String code;
    
    @NotBlank(message = "权限名称不能为空")
    private String name;
    
    private String description;
}
