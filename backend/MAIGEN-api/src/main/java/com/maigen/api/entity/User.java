package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "username")
    private String username;

    /**
     * 
     */
    @TableField(value = "password")
    private String password;

    /**
     * 
     */
    @TableField(value = "email")
    private String email;

    /**
     * 
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 
     */
    @TableField(value = "invitation_code")
    private String invitation_code;

    /**
     * 
     */
    @TableField(value = "points")
    private Integer points;

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
    @TableField(value = "updated_at")
    private LocalDateTime updated_at;
}