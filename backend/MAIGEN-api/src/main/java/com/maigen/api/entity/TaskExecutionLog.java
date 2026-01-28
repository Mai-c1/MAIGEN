package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName task_execution_log
 */
@TableName(value ="task_execution_log")
@Data
public class TaskExecutionLog {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "task_id")
    private Long task_id;

    /**
     * 
     */
    @TableField(value = "ai_role")
    private String ai_role;

    /**
     * 
     */
    @TableField(value = "ai_response")
    private String ai_response;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime created_at;
}