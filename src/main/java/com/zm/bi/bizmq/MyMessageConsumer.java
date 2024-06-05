package com.zm.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.zm.bi.manager.AiManager;
import com.zm.bi.model.entity.Chart;
import com.zm.bi.service.ChartService;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyMessageConsumer {




    //指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        System.out.println("接收到消息：" + message);
        channel.basicAck(deliveryTag,false);
    }


}
