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
    private Long user_id;

    /**
     * 
     */
    @TableField(value = "title")
    private String title;

    /**
     * 
     */
    @TableField(value = "problem_description")
    private String problem_description;

    /**
     * 
     */
    @TableField(value = "standard_code")
    private String standard_code;

    /**
     * 
     */
    @TableField(value = "testcase_count")
    private Integer testcase_count;

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
    @TableField(value = "total_points")
    private Integer total_points;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime created_at;

    /**
     * 
     */
    @TableField(value = "updated_at")
    private LocalDateTime updated_at;

    /**
     * 
     */
    @TableField(value = "expired_at")
    private LocalDateTime expired_at;
}