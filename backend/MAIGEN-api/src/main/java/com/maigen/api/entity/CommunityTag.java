package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName community_tag
 */
@TableName(value ="community_tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityTag {
    /**
     * 
     */
    @TableField(value = "community_id")
    private Long communityId;

    /**
     * 
     */
    @TableField(value = "tag_id")
    private Long tagId;
}