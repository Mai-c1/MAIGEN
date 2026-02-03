package com.maigen.api.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审核操作 DTO")
public class AuditDTO {
    @Schema(description = "审核状态 (PASS/REJECT)")
    private String status;

    @Schema(description = "审核备注")
    private String reason;
}
