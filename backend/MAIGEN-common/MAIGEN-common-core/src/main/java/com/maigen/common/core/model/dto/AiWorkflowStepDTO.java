package com.maigen.common.core.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class AiWorkflowStepDTO implements Serializable {
    private Long id;
    private Long workflowId;
    private Integer stepOrder;
    private String roleName;
    private String systemPrompt;
    private String userPromptTemplate;
}
