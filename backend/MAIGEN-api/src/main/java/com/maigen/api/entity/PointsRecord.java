package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName points_record
 */
@TableName(value ="points_record")
@Data
public class PointsRecord {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 
     */
    @TableField(value = "amount")
    private Integer amount;

    /**
     * 
     */
    @TableField(value = "source")
    private String source;

    /**
     * 
     */
    @TableField(value = "related_id")
    private String related_id;

    /**
     * 
     */
    @TableField(value = "description")
    private String description;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime created_at;
}