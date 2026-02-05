package com.maigen.api.model.dto.admin;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class RoleCreateDTO {
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    private String description;
    
    private List<Long> permissionIds;
}
