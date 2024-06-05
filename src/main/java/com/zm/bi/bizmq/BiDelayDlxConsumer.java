package com.zm.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.zm.bi.manager.TaskManager;
import com.zm.bi.model.entity.Chart;
import com.zm.bi.service.ChartService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.concurrent.Future;

import static com.zm.bi.constant.RabbitMQConstant.BI_DELAY_DLX_QUEUE_NAME;

/**
 * 延迟队列的消息到期了会发送到该死信队列
 */
@Component
public class BiDelayDlxConsumer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private TaskManager taskManager;

    @Resource
    private ChartService chartService;

    @RabbitListener(queues = {BI_DELAY_DLX_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Future<?> future = taskManager.removeTask(message);
        if(future != null && !future.isDone()){
            future.cancel(true);
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            chart.setStatus("failed");
            boolean b = chartService.updateById(chart);
        }
    }
}
