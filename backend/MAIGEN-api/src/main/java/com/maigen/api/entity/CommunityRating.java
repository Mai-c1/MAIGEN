package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName community_rating
 */
@TableName(value ="community_rating")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityRating {
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
    @TableField(value = "community_id")
    private Long communityId;

    /**
     * 
     */
    @TableField(value = "score")
    private Integer score;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}