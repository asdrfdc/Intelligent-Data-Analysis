package com.zm.bi.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.zm.bi.constant.RabbitMQConstant.BI_DELAY_EXCHANGE_NAME;
import static com.zm.bi.constant.RabbitMQConstant.BI_DELAY_ROUTING_KEY;

@Component
public class BiDelayMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BI_DELAY_EXCHANGE_NAME, BI_DELAY_ROUTING_KEY, message);
    }
}
