package com.maigen.common.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionLogDTO implements Serializable {
    private Long taskId;
    private Integer stepOrder;
    private String roleName;
    private String promptSnapshot;
    private String aiResponse;
    private LocalDateTime createTime;
}
