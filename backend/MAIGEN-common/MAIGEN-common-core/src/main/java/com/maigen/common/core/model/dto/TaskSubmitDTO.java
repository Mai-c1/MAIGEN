package com.maigen.common.core.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskSubmitDTO implements Serializable {
    private Long taskId;
    private Long userId;
    private String title;
    private String problemDescription;
    private String standardCode;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer testcaseCount;
    private Long workflowId;
}
