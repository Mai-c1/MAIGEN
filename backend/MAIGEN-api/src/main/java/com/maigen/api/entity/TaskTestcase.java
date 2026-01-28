package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName task_testcase
 */
@TableName(value ="task_testcase")
@Data
public class TaskTestcase {
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
    @TableField(value = "testcase_index")
    private Integer testcase_index;

    /**
     * 
     */
    @TableField(value = "strategy_id")
    private Long strategy_id;

    /**
     * 
     */
    @TableField(value = "parameters")
    private Object parameters;

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