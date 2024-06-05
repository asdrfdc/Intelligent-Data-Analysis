package com.zm.bi.bizmq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.zm.bi.constant.RabbitMQConstant.BI_EXCHANGE_NAME;
import static com.zm.bi.constant.RabbitMQConstant.BI_ROUTING_KEY;

@Component
public class BiMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  发送消息
     * @param message
     */
    public void sendMessage( String message) {
        //1.正常发送消息
        rabbitTemplate.convertAndSend(BI_EXCHANGE_NAME, BI_ROUTING_KEY, message);
        //2.延迟发送消息
        rabbitTemplate.convertAndSend(BI_EXCHANGE_NAME, BI_ROUTING_KEY, message,
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        message.getMessageProperties().setExpiration("15000");
                        return message;
                    }
                });
        //3.延迟发送消息的lambda
        rabbitTemplate.convertAndSend(BI_EXCHANGE_NAME, BI_ROUTING_KEY, message, message1 -> {
            message1.getMessageProperties().setExpiration("15000");
            return message1;
        });
    }
}
