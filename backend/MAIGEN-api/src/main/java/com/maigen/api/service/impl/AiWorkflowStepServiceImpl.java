package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.AiWorkflowStep;
import com.maigen.api.mapper.AiWorkflowStepMapper;
import com.maigen.api.service.AiWorkflowStepService;
import org.springframework.stereotype.Service;

@Service
public class AiWorkflowStepServiceImpl extends ServiceImpl<AiWorkflowStepMapper, AiWorkflowStep> implements AiWorkflowStepService {
}
