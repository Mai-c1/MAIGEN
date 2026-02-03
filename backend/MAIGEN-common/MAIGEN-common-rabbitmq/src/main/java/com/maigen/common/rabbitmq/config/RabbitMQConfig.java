package com.maigen.common.rabbitmq.config;

import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类
 */
@Configuration
@PropertySource("classpath:rabbitmq.properties")
public class RabbitMQConfig {

    // --- Exchange 定义 ---

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(RabbitMQConstants.EXCHANGE_TASK, true, false);
    }

    @Bean
    public DirectExchange taskDlxExchange() {
        return new DirectExchange(RabbitMQConstants.EXCHANGE_TASK_DLX, true, false);
    }

    // --- Queue 定义 (带死信配置) ---

    @Bean
    public Queue submitQueue() {
        return QueueBuilder.durable(RabbitMQConstants.QUEUE_TASK_SUBMIT)
                .deadLetterExchange(RabbitMQConstants.EXCHANGE_TASK_DLX)
                .deadLetterRoutingKey(RabbitMQConstants.QUEUE_TASK_DEAD)
                .build();
    }

    @Bean
    public Queue executeQueue() {
        return QueueBuilder.durable(RabbitMQConstants.QUEUE_TASK_EXECUTE)
                .deadLetterExchange(RabbitMQConstants.EXCHANGE_TASK_DLX)
                .deadLetterRoutingKey(RabbitMQConstants.QUEUE_TASK_DEAD)
                .build();
    }

    @Bean
    public Queue updateQueue() {
        return QueueBuilder.durable(RabbitMQConstants.QUEUE_TASK_UPDATE)
                .deadLetterExchange(RabbitMQConstants.EXCHANGE_TASK_DLX)
                .deadLetterRoutingKey(RabbitMQConstants.QUEUE_TASK_DEAD)
                .build();
    }

    @Bean
    public Queue statusQueue() {
        return QueueBuilder.durable(RabbitMQConstants.QUEUE_TASK_STATUS)
                .deadLetterExchange(RabbitMQConstants.EXCHANGE_TASK_DLX)
                .deadLetterRoutingKey(RabbitMQConstants.QUEUE_TASK_DEAD)
                .build();
    }

    @Bean
    public Queue deadQueue() {
        return new Queue(RabbitMQConstants.QUEUE_TASK_DEAD, true);
    }

    // --- Binding 定义 ---

    @Bean
    public Binding bindSubmit() {
        return BindingBuilder.bind(submitQueue()).to(taskExchange()).with(RabbitMQConstants.QUEUE_TASK_SUBMIT);
    }

    @Bean
    public Binding bindExecute() {
        return BindingBuilder.bind(executeQueue()).to(taskExchange()).with(RabbitMQConstants.QUEUE_TASK_EXECUTE);
    }

    @Bean
    public Binding bindUpdate() {
        return BindingBuilder.bind(updateQueue()).to(taskExchange()).with(RabbitMQConstants.QUEUE_TASK_UPDATE);
    }

    @Bean
    public Binding bindStatus() {
        return BindingBuilder.bind(statusQueue()).to(taskExchange()).with(RabbitMQConstants.QUEUE_TASK_STATUS);
    }

    @Bean
    public Binding bindDead() {
        return BindingBuilder.bind(deadQueue()).to(taskDlxExchange()).with(RabbitMQConstants.QUEUE_TASK_DEAD);
    }

    /**
     * 消息转换器：使用 JSON 序列化
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 默认队列 (保留兼容)
     */
    @Bean
    public Queue defaultQueue() {
        return new Queue("default.queue", true);
    }
}