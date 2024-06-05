package com.zm.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import static com.zm.bi.constant.RabbitMQConstant.*;

/**
 * 用于创建测试程序用到的队列和交换机（只用在程序执行前执行一次）
 */
public class BiInitMain {

    public static void main(String[] args) {

        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = BI_EXCHANGE_NAME;
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            //创建队列
            String queueName = BI_QUEUE_NAME;
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, BI_ROUTING_KEY);
        }catch (Exception e){

        }
    }
}