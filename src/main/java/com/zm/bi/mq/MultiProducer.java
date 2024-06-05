package com.zm.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.ietf.jgss.MessageProp;

import java.util.Scanner;

public class MultiProducer {

    private final static String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try(Connection connection = factory.newConnection();
            Channel channel=connection.createChannel()){
            channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);

            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String message = scanner.nextLine();
                channel.basicPublish("",TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }
}
