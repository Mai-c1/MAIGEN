package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName user_sign_in
 */
@TableName(value ="user_sign_in")
@Data
public class UserSignIn {
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
    @TableField(value = "sign_in_date")
    private LocalDate signInDate;

    /**
     * 
     */
    @TableField(value = "points_reward")
    private Integer pointsReward;

    /**
     * 连续签到天数
     */
    @TableField(value = "continuous_days")
    private Integer continuousDays;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}