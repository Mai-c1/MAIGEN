package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "社区内容查询参数")
public class CommunityQuery extends PageQuery {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "排序字段: newest, popular_like, popular_download", example = "newest")
    private String orderBy = "newest";
}
