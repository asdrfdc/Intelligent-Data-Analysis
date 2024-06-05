package com.zm.bi.constant;

public interface RabbitMQConstant {

    //工作——队列、交换机、routingKey
    public static final String BI_EXCHANGE_NAME = "bi_exchange";

    public static final String BI_QUEUE_NAME = "bi_queue";

    public static final String BI_ROUTING_KEY = "bi_routingKey";


    //死信——队列、交换机、routingKey
    public static final String BI_DLX_EXCHANGE_NAME = "bi_dlx_exchange";

    public static final String BI_DLX_QUEUE_NAME = "bi_dlx_queue";

    public static final String BI_DLX_ROUTING_KEY = "bi_dlx_routingKey";


    //延迟——队列、交换机、routingKey
    public static final String BI_DELAY_EXCHANGE_NAME = "bi_delay_exchange";

    public static final String BI_DELAY_QUEUE_NAME = "bi_delay_queue";

    public static final String BI_DELAY_ROUTING_KEY = "bi_delay_routingKey";


    //延迟——死信——队列、交换机、routingKey
    public static final String BI_DELAY_DLX_EXCHANGE_NAME = "bi_delay_dlx_exchange";

    public static final String BI_DELAY_DLX_QUEUE_NAME = "bi_delay_dlx_queue";

    public static final String BI_DELAY_DLX_ROUTING_KEY = "bi_delay_dlx_routingKey";
}
