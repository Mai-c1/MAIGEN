package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "积分变动记录VO")
public class PointsRecordVO {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "变动金额")
    private Integer amount;

    @Schema(description = "变动来源")
    private String source;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "关联ID")
    private String relatedId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
