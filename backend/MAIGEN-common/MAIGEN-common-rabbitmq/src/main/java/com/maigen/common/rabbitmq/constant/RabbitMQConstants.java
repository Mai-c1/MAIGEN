package com.maigen.common.rabbitmq.constant;

/**
 * RabbitMQ 常量定义
 */
public class RabbitMQConstants {

    /**
     * 任务交换机
     */
    public static final String EXCHANGE_TASK = "maigen.task.exchange";

    /**
     * 任务死信交换机
     */
    public static final String EXCHANGE_TASK_DLX = "maigen.task.exchange.dlx";

    /**
     * 任务提交队列 (API -> Analysis)
     */
    public static final String QUEUE_TASK_SUBMIT = "maigen.task.submit";

    /**
     * 任务提交路由键
     */
    public static final String ROUTING_TASK_SUBMIT = "maigen.task.submit";

    /**
     * 任务下发执行队列 (Analysis -> Sandbox)
     */
    public static final String QUEUE_TASK_EXECUTE = "maigen.task.execute";

    /**
     * 任务结果回传队列 (Sandbox -> API)
     */
    public static final String QUEUE_TASK_UPDATE = "maigen.task.update";

    /**
     * 任务状态更新队列 (Worker -> API)
     */
    public static final String QUEUE_TASK_STATUS = "maigen.task.status";

    /**
     * 任务全链路死信队列
     */
    public static final String QUEUE_TASK_DEAD = "maigen.task.dead";
}