package com.maigen.common.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务提交 DTO (API -> Analysis)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmitDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;
}