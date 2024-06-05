package com.zm.bi.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import lombok.var;

import java.nio.charset.StandardCharsets;

public class TtlProducer {

    private static final String QUEUE_NAME = "ttl_queue";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        //建立连接，创建频道
        try(var channel = factory.newConnection().createChannel()){
            //channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "hello world";

            //给消息指定过期时间
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration("10000")
                            .build();
            channel.basicPublish("", QUEUE_NAME, properties, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
