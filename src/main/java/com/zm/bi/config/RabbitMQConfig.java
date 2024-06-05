package com.zm.bi.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import static com.zm.bi.constant.RabbitMQConstant.*;

@Configuration
public class RabbitMQConfig {

    /**
     * 工作队列
     * @return
     */
    @Bean
    public Queue biQueue(){
        return QueueBuilder.durable(BI_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", BI_DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", BI_DLX_ROUTING_KEY)
                .withArgument("x-message-ttl", 15*1000)
                .build();
    }

    /**
     * 工作交换机
     * @return
     */
    @Bean
    public DirectExchange biExchange(){
        return new DirectExchange(BI_EXCHANGE_NAME,true,false);
    }

    /**
     * 绑定工作队列与交换机
     * @return
     */
    @Bean
    public Binding biBinding(){
        return BindingBuilder.bind(biQueue()).to(biExchange()).with(BI_ROUTING_KEY);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue biDlxQueue(){
        return QueueBuilder.durable(BI_DLX_QUEUE_NAME)
                .build();
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean
    public DirectExchange biDlxExchange(){
        return new DirectExchange(BI_DLX_EXCHANGE_NAME,true,false);
    }

    /**
     * 绑定死信队列与死信交换机
     * @return
     */
    @Bean
    public Binding biDlxBinding(){
        return BindingBuilder.bind(biDlxQueue()).to(biDlxExchange()).with(BI_DLX_ROUTING_KEY);
    }

    /**
     * 延迟队列用于实现超时控制
     * @return
     */
    @Bean
    public Queue biDelayQueue(){
        return QueueBuilder.durable(BI_DELAY_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", BI_DELAY_DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", BI_DELAY_DLX_ROUTING_KEY)
                .withArgument("x-message-ttl", 15*1000)
                .build();
    }

    /**
     * 延迟交换机
     * @return
     */
    @Bean
    public DirectExchange biDelayExchange(){
        return new DirectExchange(BI_DELAY_EXCHANGE_NAME,true,false);
    }

    /**
     * 绑定延迟队列与延迟交换机
     * @return
     */
    @Bean
    public Binding biDelayBinding(){
        return BindingBuilder.bind(biDelayQueue()).to(biDelayExchange()).with(BI_DELAY_ROUTING_KEY);
    }

    /**
     * 延迟死信队列
     * @return
     */
    @Bean
    public Queue biDelayDlxQueue(){
        return QueueBuilder.durable(BI_DELAY_DLX_QUEUE_NAME)
                .build();
    }

    /**
     * 延迟死信交换机
     * @return
     */
    @Bean
    public DirectExchange biDelayDlxExchange(){
        return new DirectExchange(BI_DELAY_DLX_EXCHANGE_NAME,true,false);
    }

    /**
     * 绑定延迟死信队列与延迟死信交换机
     * @return
     */
    @Bean
    public Binding biDelayDlxBinding(){
        return BindingBuilder.bind(biDelayDlxQueue()).to(biDelayDlxExchange()).with(BI_DELAY_DLX_ROUTING_KEY);
    }
}
