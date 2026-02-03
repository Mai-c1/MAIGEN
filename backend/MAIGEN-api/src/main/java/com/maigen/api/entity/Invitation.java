package com.maigen.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName invitation
 */
@TableName(value ="invitation")
@Data
public class Invitation {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "inviter_id")
    private Long inviterId;

    /**
     * 
     */
    @TableField(value = "invitee_id")
    private Long inviteeId;

    /**
     * 
     */
    @TableField(value = "invitation_code")
    private String invitationCode;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}