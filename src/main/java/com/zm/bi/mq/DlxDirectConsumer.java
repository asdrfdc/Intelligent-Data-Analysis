package com.zm.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;

public class DlxDirectConsumer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";

    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";

    public static void main(String[] argv) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明工作队列
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        //指定死信队列参数
        HashMap<String, Object> args = new HashMap<>();
        //绑定死信交换机
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        //绑定死信队列
        args.put("x-dead-letter-routing-key", "waibao");

        //创建工作队列小狗
        String queueName= "xiaodog_queue";
        //声明工作队列的同时绑定私信交换机
        channel.queueDeclare(queueName, true, false, false, args);
        //绑定工作交换机
        channel.queueBind(queueName, WORK_EXCHANGE_NAME, "xiaodog");

        HashMap<String, Object> args2 = new HashMap<>();
        //绑定死信交换机
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        //绑定死信队列
        args2.put("x-dead-letter-routing-key", "laoban");

        //创建工作队列小猫
        String queueName2= "xiaocat_queue";
        //声明工作队列的同时绑定私信交换机
        channel.queueDeclare(queueName2, true, false, false, args2);
        //绑定工作交换机
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "xiaocat");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback xiaodogCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [xiaodog] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        DeliverCallback xiaocatCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [xiaocat] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, xiaodogCallback, consumerTag -> { });
        channel.basicConsume(queueName2, true, xiaocatCallback, consumerTag -> { });

    }
}
