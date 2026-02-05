package com.maigen.api.model.dto.admin;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class RoleUpdateDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    
    private String name;
    
    private String description;
    
    private List<Long> permissionIds;
}
