package com.zm.bi.mq;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.var;

import java.util.Scanner;

public class DlxDirectProducer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";

    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try(var connection = factory.newConnection()){
            var channel = connection.createChannel();
            //声明死信交换机
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

            //声明死信队列(和正常声明队列没什么区别)
            //老板死信任务队列
            String queueName = "laoban_dlx_queue";
            channel.queueDeclare(queueName,true,false,false,null);
            //死信队列绑定交换机
            channel.queueBind(queueName,DEAD_EXCHANGE_NAME,"laoban");

            //外包死信任务队列
            String queueName2="waibao_dlx_queue";
            channel.queueDeclare(queueName2,true,false,false,null);
            //死信队列绑定交换机
            channel.queueBind(queueName2,DEAD_EXCHANGE_NAME,"waibao");

            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String userInput = scanner.nextLine();
                String[] strings = userInput.split(" ");
                if(strings.length < 2){
                    continue;
                }
                String message = strings[0];
                String routingKey = strings[1];

                //向工作队列发消息，工作队列拒收后会转到死信队列
                channel.basicPublish(WORK_EXCHANGE_NAME,routingKey,null,message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "with routingKey:" + routingKey + "'");
            }


            DeliverCallback laobanDeliveryCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                //拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                System.out.println(" [laoban] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };

            DeliverCallback waibaoDeliveryCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                //拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                System.out.println(" [waibao] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };

            channel.basicConsume(queueName, true, laobanDeliveryCallback, consumerTag -> { });
            channel.basicConsume(queueName2, true, waibaoDeliveryCallback, consumerTag -> { });
        }
    }
}
