package com.maigen.api.listener;

import com.maigen.api.service.SystemConfigService;
import com.maigen.api.service.AiWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 缓存预热监听器
 * 在应用启动就绪后，将必要数据加载到 Redis 缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmupListener {

    private final SystemConfigService systemConfigService;
    private final AiWorkflowService workflowService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        log.info("开始进行系统缓存预热...");
        try {
            // 1. 预热系统配置
            systemConfigService.refreshAllCache();
            log.info("系统配置缓存预热完成");
            
            // 2. 预热 AI 工作流配置
            workflowService.syncCache();
            log.info("AI工作流配置缓存预热完成");
            
        } catch (Exception e) {
            log.error("系统缓存预热失败", e);
        }
    }
}
