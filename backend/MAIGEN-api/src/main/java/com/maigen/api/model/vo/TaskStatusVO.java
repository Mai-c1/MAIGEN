package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "任务状态信息")
public class TaskStatusVO implements Serializable {

    @Schema(description = "任务ID", example = "123456789")
    private Long taskId;

    @Schema(description = "任务标题", example = "快速排序测试数据生成")
    private String title;

    @Schema(description = "任务状态 (0-待处理, 1-分析中, 2-生成中, 3-测试中, 4-完成, 5-失败)", example = "4")
    private Integer status; // 0-待处理, 1-分析中, 2-生成中, 3-测试中, 4-完成, 5-失败

    @Schema(description = "进度百分比 (0-100)", example = "85")
    private Integer progress;

    @Schema(description = "结果下载链接", example = "https://minio.example.com/result.zip")
    private String resultUrl; // 下载链接 (如果完成)

    @Schema(description = "错误信息", example = "Syntax Error")
    private String errorMessage; // 错误信息 (如果失败)

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:05:00")
    private LocalDateTime updateTime;
}
