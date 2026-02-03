package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName task
 */
@TableName(value ="task")
@Data
public class Task {
    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 
     */
    @TableField(value = "title")
    private String title;

    /**
     * 
     */
    @TableField(value = "problem_description")
    private String problemDescription;

    /**
     * 
     */
    @TableField(value = "standard_code")
    private String standardCode;

    /**
     * 
     */
    @TableField(value = "testcase_count")
    private Integer testcaseCount;

    /**
     * 
     */
    @TableField(value = "time_limit")
    private Integer timeLimit;

    /**
     * 
     */
    @TableField(value = "memory_limit")
    private Integer memoryLimit;

    /**
     * 测试点策略列表 (非数据库字段)
     */
    @TableField(exist = false)
    private java.util.List<TaskStrategy> strategyList;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "progress")
    private Integer progress;

    /**
     * 
     */
    @TableField(value = "retry_count")
    private Integer retryCount;

    /**
     * 
     */
    @TableField(value = "error_message")
    private String errorMessage;

    /**
     * 
     */
    @TableField(value = "total_points")
    private Integer totalPoints;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 
     */
    @TableField(value = "expired_at")
    private LocalDateTime expiredAt;
}