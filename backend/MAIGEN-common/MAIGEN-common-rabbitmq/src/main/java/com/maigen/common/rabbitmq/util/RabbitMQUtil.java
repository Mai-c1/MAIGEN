package com.maigen.common.rabbitmq.util;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQ工具类
 */
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQUtil(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送消息
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送消息到队列
     */
    public void sendMessageToQueue(String queueName, Object message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}