package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分享社区内容请求")
public class CommunityShareDTO {

    @Schema(description = "标题", example = "快速排序测试数据")
    private String title;

    @Schema(description = "描述", example = "包含极值、逆序等多种情况")
    private String description;

    @Schema(description = "数据文件路径 (MinIO)", example = "/data/result_123.zip")
    private String dataFilePath;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "标签列表")
    private List<Long> tagIds;

    @Schema(description = "所需积分", example = "10")
    private Integer points;
}
