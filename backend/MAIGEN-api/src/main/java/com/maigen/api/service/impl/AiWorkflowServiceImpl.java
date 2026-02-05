package com.maigen.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.AiWorkflow;
import com.maigen.api.entity.AiWorkflowStep;
import com.maigen.api.mapper.AiWorkflowMapper;
import com.maigen.api.service.AiWorkflowService;
import com.maigen.api.service.AiWorkflowStepService;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiWorkflowServiceImpl extends ServiceImpl<AiWorkflowMapper, AiWorkflow> implements AiWorkflowService {

    private final AiWorkflowStepService stepService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyWorkflow(Long workflowId) {
        // 1. 获取原方案
        AiWorkflow original = this.getById(workflowId);
        if (original == null) {
            throw new RuntimeException("方案不存在");
        }

        // 2. 复制方案主体
        AiWorkflow copy = new AiWorkflow();
        BeanUtil.copyProperties(original, copy, "id", "createdAt", "updatedAt");
        copy.setName(original.getName() + " - 副本");
        copy.setIsVisible(false); // 默认不可见
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());
        this.save(copy);

        // 3. 复制步骤
        List<AiWorkflowStep> steps = stepService.list(
                new LambdaQueryWrapper<AiWorkflowStep>().eq(AiWorkflowStep::getWorkflowId, workflowId)
        );
        
        List<AiWorkflowStep> newSteps = steps.stream().map(step -> {
            AiWorkflowStep newStep = new AiWorkflowStep();
            BeanUtil.copyProperties(step, newStep, "id");
            newStep.setWorkflowId(copy.getId());
            return newStep;
        }).collect(Collectors.toList());

        if (!newSteps.isEmpty()) {
            // 批量插入步骤
            stepService.saveBatch(newSteps);
        }
        
        // 4. 同步缓存
        syncCache();
    }

    @Override
    public List<AiWorkflowStep> getSteps(Long workflowId) {
        return stepService.list(
                new LambdaQueryWrapper<AiWorkflowStep>()
                        .eq(AiWorkflowStep::getWorkflowId, workflowId)
                        .orderByAsc(AiWorkflowStep::getStepOrder)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSteps(Long workflowId, List<AiWorkflowStep> steps) {
        // 1. 删除旧步骤
        stepService.remove(new LambdaQueryWrapper<AiWorkflowStep>().eq(AiWorkflowStep::getWorkflowId, workflowId));

        // 2. 插入新步骤
        for (AiWorkflowStep step : steps) {
            step.setWorkflowId(workflowId);
        }
        stepService.saveBatch(steps);

        // 3. 同步缓存
        syncCache();
    }

    @Override
    public void syncCache() {
        // 1. 获取所有方案
        List<AiWorkflow> allWorkflows = this.list();
        
        // 2. 准备 Hash 数据
        Map<String, String> workflowMap = new HashMap<>();
        for (AiWorkflow wf : allWorkflows) {
            List<AiWorkflowStep> steps = getSteps(wf.getId());
            workflowMap.put(wf.getId().toString(), JSONUtil.toJsonStr(steps));
        }
        
        // 3. 写入 Redis Hash
        if (!workflowMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(RedisConstants.SYS_AI_WORKFLOWS_KEY, workflowMap);
        }
        
        // 4. 缓存可见方案列表
        List<AiWorkflow> publicList = allWorkflows.stream()
                .filter(AiWorkflow::getIsVisible)
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(RedisConstants.SYS_AI_WORKFLOWS_PUBLIC_KEY, JSONUtil.toJsonStr(publicList));
    }

    @Override
    public List<AiWorkflow> getPublicWorkflows() {
        // 优先从缓存读取
        String json = redisTemplate.opsForValue().get(RedisConstants.SYS_AI_WORKFLOWS_PUBLIC_KEY);
        if (json != null) {
            return JSONUtil.toList(json, AiWorkflow.class);
        }
        // 缓存未命中则查库
        return this.list(new LambdaQueryWrapper<AiWorkflow>().eq(AiWorkflow::getIsVisible, true));
    }
}
