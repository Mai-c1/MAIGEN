package com.maigen.common.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公共实体类
 */
@Data
public class CommonEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    private Integer deleted;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}