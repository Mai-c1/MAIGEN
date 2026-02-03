package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.SystemConfig;
import com.maigen.api.service.SystemConfigService;
import com.maigen.api.mapper.SystemConfigMapper;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
* @author 25128
* @description 针对表【system_config】的数据库操作Service实现
* @createDate 2026-01-29 19:57:59
*/
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
    implements SystemConfigService{

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean updateById(SystemConfig entity) {
        boolean success = super.updateById(entity);
        if (success) {
            // 更新成功后刷新缓存
            SystemConfig config = super.getById(entity.getId());
            if (config != null) {
                refreshCache(config.getCode());
            }
        }
        return success;
    }

    @Override
    public void refreshAllCache() {
        List<SystemConfig> list = this.list();
        for (SystemConfig config : list) {
            String cacheKey = RedisConstants.getSystemConfigKey(config.getCode());
            redisTemplate.opsForValue().set(cacheKey, config.getValue());
        }
    }

    @Override
    public void refreshCache(String code) {
        SystemConfig config = this.lambdaQuery().eq(SystemConfig::getCode, code).one();
        if (config != null) {
            String cacheKey = RedisConstants.getSystemConfigKey(config.getCode());
            redisTemplate.opsForValue().set(cacheKey, config.getValue());
        }
    }
}




