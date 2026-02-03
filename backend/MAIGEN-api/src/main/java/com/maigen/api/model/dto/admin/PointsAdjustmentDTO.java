package com.maigen.api.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "积分调账 DTO")
public class PointsAdjustmentDTO {
    @Schema(description = "调整金额 (正数为加，负数为减)")
    private Integer amount;

    @Schema(description = "调账原因")
    private String reason;
}
