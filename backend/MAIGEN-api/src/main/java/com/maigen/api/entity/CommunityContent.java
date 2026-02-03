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
    private Long userId;

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
    private String dataFilePath;

    /**
     * 
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "view_count")
    private Integer viewCount;

    /**
     * 
     */
    @TableField(value = "download_count")
    private Integer downloadCount;

    /**
     * 
     */
    @TableField(value = "like_count")
    private Integer likeCount;

    /**
     * 
     */
    @TableField(value = "rating_avg")
    private Double ratingAvg;

    /**
     * 
     */
    @TableField(value = "rating_count")
    private Integer ratingCount;

    /**
     * 
     */
    @TableField(value = "points")
    private Integer points;

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
}