package com.maigen.common.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务结果 DTO (Sandbox -> API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 测试数据下载链接
     */
    private String downloadUrl;

    /**
     * 任务状态 (例如: 4-完成, 5-失败)
     * 具体状态码定义需与 task 表 status 保持一致
     */
    private Integer status;
    
    /**
     * 额外信息/错误信息
     */
    private String message;
}