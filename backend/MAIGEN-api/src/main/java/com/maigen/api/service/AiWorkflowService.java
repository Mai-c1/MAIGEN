package com.maigen.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maigen.api.entity.AiWorkflow;
import com.maigen.api.entity.AiWorkflowStep;
import java.util.List;

public interface AiWorkflowService extends IService<AiWorkflow> {
    
    /**
     * 复制方案
     */
    void copyWorkflow(Long workflowId);

    /**
     * 获取方案的步骤列表
     */
    List<AiWorkflowStep> getSteps(Long workflowId);

    /**
     * 保存步骤列表 (全量覆盖)
     */
    void saveSteps(Long workflowId, List<AiWorkflowStep> steps);

    /**
     * 同步缓存
     */
    void syncCache();

    /**
     * 获取前端可见的方案列表
     */
    List<AiWorkflow> getPublicWorkflows();
}
