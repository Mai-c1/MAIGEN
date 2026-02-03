package com.maigen.common.core.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class TaskResultDTO implements Serializable {
    private Long taskId;
    private boolean success;
    private String downloadUrl; // 预签名下载链接
    private String bucketName;  // MinIO 桶名
    private String objectName;  // MinIO 对象名
    private String errorMessage; // 如果失败
}
