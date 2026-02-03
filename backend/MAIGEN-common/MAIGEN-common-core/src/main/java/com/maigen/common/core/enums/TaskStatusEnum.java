package com.maigen.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PENDING(0, "待处理", "任务已提交，等待分析...", 0),
    ANALYZING(1, "分析中", "正在拉取数据并进行题目分析...", 10),
    GENERATING(2, "生成中", "分析完成，正在生成代码脚本...", 60),
    VERIFYING(3, "验证中", "代码生成完成，正在启动沙箱进行验证...", 70),
    COMPLETED(4, "已完成", "任务处理成功", 100),
    FAILED(5, "生成失败", "任务处理失败", 0),
    TIMEOUT(6, "超时", "任务处理超时", 0),
    CANCELLED(7, "已取消", "任务已取消", 0);

    private final Integer code;
    private final String desc;
    private final String defaultMessage;
    private final Integer defaultProgress;

    public static TaskStatusEnum getByCode(Integer code) {
        for (TaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * 是否为终态
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }

    /**
     * 是否正在运行中
     */
    public boolean isRunning() {
        return this == ANALYZING || this == GENERATING || this == VERIFYING;
    }
}
