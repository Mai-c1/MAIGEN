package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "社区内容详细信息")
public class CommunityDetailVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述 (Markdown)")
    private String description;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "所需积分")
    private Integer points;

    @Schema(description = "查看次数")
    private Integer viewCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "平均评分")
    private Double ratingAvg;

    @Schema(description = "评分人数")
    private Integer ratingCount;

    @Schema(description = "当前用户是否已点赞")
    private Boolean isLiked;

    @Schema(description = "当前用户是否已解锁")
    private Boolean isUnlocked;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
