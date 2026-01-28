package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName community_content
 */
@TableName(value ="community_content")
@Data
public class CommunityContent {
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
    @TableField(value = "title")
    private String title;

    /**
     * 
     */
    @TableField(value = "description")
    private String description;

    /**
     * 
     */
    @TableField(value = "data_file_path")
    private String data_file_path;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "view_count")
    private Integer view_count;

    /**
     * 
     */
    @TableField(value = "download_count")
    private Integer download_count;

    /**
     * 
     */
    @TableField(value = "points")
    private Integer points;

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
}