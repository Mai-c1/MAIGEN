package com.maigen.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "创建任务请求参数")
public class CreateTaskDTO implements Serializable {

    @Schema(description = "任务标题", example = "A+B Problem生成")
    private String title;

    @Schema(description = "题目描述 (Markdown格式)", example = "请生成一道计算两个整数和的题目...")
    private String problemDescription; // 题目描述 (Markdown)

    @Schema(description = "标程代码 (C++)", example = "#include <iostream>...")
    private String standardCode; // 标程 (C++)

    @Schema(description = "时间限制 (ms)", example = "1000")
    private Integer timeLimit;

    @Schema(description = "内存限制 (MB)", example = "256")
    private Integer memoryLimit;

    @Schema(description = "测试点数量 (1-50)", example = "10")
    private Integer testcaseCount; // 测试点数量 (1-50)

    @Schema(description = "选中的生成方案ID", example = "1")
    private Long workflowId;
}
