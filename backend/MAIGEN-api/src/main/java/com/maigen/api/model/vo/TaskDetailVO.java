package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "任务详细信息")
public class TaskDetailVO implements Serializable {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "题目描述 (Markdown)")
    private String problemDescription;

    @Schema(description = "标准程序 (C++)")
    private String standardCode;

    @Schema(description = "测试点数量")
    private Integer testcaseCount;

    @Schema(description = "时间限制 (ms)")
    private Integer timeLimit;

    @Schema(description = "空间限制 (MB)")
    private Integer memoryLimit;

    @Schema(description = "策略列表")
    private List<String> strategies;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "结果下载链接")
    private String resultUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
