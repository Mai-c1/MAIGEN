package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI工作流步骤
 * @TableName ai_workflow_step
 */
@TableName(value ="ai_workflow_step")
@Data
public class AiWorkflowStep {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工作流ID
     */
    @TableField(value = "workflow_id")
    private Long workflowId;

    /**
     * 执行顺序
     */
    @TableField(value = "step_order")
    private Integer stepOrder;

    /**
     * AI角色名称
     */
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 系统提示词
     */
    @TableField(value = "system_prompt")
    private String systemPrompt;

    /**
     * 用户提示词模板
     */
    @TableField(value = "user_prompt_template")
    private String userPromptTemplate;
}
