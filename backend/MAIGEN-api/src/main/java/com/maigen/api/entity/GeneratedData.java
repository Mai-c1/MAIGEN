package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName generated_data
 */
@TableName(value ="generated_data")
@Data
public class GeneratedData {
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
    @TableField(value = "file_name")
    private String file_name;

    /**
     * 
     */
    @TableField(value = "file_path")
    private String file_path;

    /**
     * 
     */
    @TableField(value = "download_url")
    private String download_url;

    /**
     * 
     */
    @TableField(value = "size")
    private Long size;

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

    /**
     * 
     */
    @TableField(value = "expired_at")
    private LocalDateTime expired_at;
}