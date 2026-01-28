package com.maigen.common.rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 默认队列
     */
    @Bean
    public Queue defaultQueue() {
        return new Queue("default.queue", true);
    }

    /**
     * 业务队列
     */
    @Bean
    public Queue businessQueue() {
        return new Queue("business.queue", true);
    }
}