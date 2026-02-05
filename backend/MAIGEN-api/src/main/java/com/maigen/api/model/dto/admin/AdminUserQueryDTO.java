package com.maigen.api.model.dto.admin;

import com.maigen.api.model.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryDTO extends PageQuery {
    private String keyword;
    private Integer status;
    private String role; // 角色标识: admin, user
    private Long minPoints;
    private Long maxPoints;
}
