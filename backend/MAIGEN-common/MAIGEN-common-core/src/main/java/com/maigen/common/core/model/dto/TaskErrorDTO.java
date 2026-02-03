package com.maigen.common.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务异常信息 DTO (用于死信队列)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskErrorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 错误信息
     */
    private String errorMessage;
}
