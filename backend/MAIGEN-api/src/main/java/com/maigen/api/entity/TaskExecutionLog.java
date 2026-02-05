package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 任务执行日志
 * @TableName task_execution_log
 */
@TableName(value ="task_execution_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionLog {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的任务ID
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 步骤顺序
     */
    @TableField(value = "step_order")
    private Integer stepOrder;

    /**
     * AI角色名称
     */
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 发送给AI的Prompt快照
     */
    @TableField(value = "prompt_snapshot")
    private String promptSnapshot;

    /**
     * AI的响应内容
     */
    @TableField(value = "ai_response")
    private String aiResponse;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
