package com.zm.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TtlConsumer {

    private static final String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //创建队列，指定消息过期参数
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-message-ttl",5000);
        //给队列指定过期时间
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //定义了如何处理消息
        DeliverCallback deliverCallback=(consumerTag,delivery)->{
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        //消费消息，会持续阻塞
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }
}
