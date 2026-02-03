package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "分类信息")
public class CategoryVO {
    private Long id;
    private String name;
    private String description;
}
