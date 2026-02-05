package com.maigen.api.model.dto.admin;

import com.maigen.api.model.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageQuery {
    private String keyword;
}
