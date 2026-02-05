package com.maigen.api.model.dto.admin;

import com.maigen.api.model.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogQueryDTO extends PageQuery {
    private String module;
    private Long userId;
    private Integer status;
    private String startDate;
    private String endDate;
}
