package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Schema(description = "任务统计信息")
public class TaskStatisticsVO implements Serializable {

    @Schema(description = "进行中的任务数 (待处理/分析中/生成中/验证中)")
    private Long inProgressCount;

    @Schema(description = "已完成的任务数")
    private Long completedCount;

    @Schema(description = "失败的任务数 (生成失败/超时)")
    private Long failedCount;

    @Schema(description = "总任务数")
    private Long totalCount;
}
