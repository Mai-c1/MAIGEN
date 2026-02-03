package com.maigen.common.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusDTO implements Serializable {
    private Long taskId;
    private Integer status;
    private Integer progress;
    private String message;
}
