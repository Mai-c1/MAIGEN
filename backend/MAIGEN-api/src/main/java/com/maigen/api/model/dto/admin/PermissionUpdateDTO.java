package com.maigen.api.model.dto.admin;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class PermissionUpdateDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    
    private String code;
    private String name;
    private String description;
}
