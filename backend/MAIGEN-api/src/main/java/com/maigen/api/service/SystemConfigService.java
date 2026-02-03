package com.maigen.api.service;

import com.maigen.api.entity.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 25128
* @description 针对表【system_config】的数据库操作Service
* @createDate 2026-01-29 19:57:59
*/
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 刷新所有配置到 Redis 缓存
     */
    void refreshAllCache();

    /**
     * 刷新单个配置到 Redis 缓存
     * @param code 配置编码
     */
    void refreshCache(String code);
}
