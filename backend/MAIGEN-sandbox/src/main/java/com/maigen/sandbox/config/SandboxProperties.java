package com.maigen.sandbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 沙箱配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "maigen.sandbox")
public class SandboxProperties {

    /**
     * go-judge 服务地址
     */
    private String serverUrl;

    /**
     * 宿主机物理路径，挂载至沙箱 /output/
     */
    private String shareDataPath;
}
