package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "社区评分请求")
public class CommunityRatingDTO {

    @Schema(description = "内容ID")
    private Long communityId;

    @Schema(description = "评分 (1-5)", example = "5")
    @Min(1)
    @Max(5)
    private Integer score;
}
