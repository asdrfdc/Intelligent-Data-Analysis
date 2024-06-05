package com.zm.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.zm.bi.common.ErrorCode;
import com.zm.bi.exception.BusinessException;
import com.zm.bi.model.entity.Chart;
import com.zm.bi.service.ChartService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.IOException;

import static com.zm.bi.constant.RabbitMQConstant.BI_DLX_QUEUE_NAME;


@Component
public class BiDlxConsumer {

    @Resource
    private ChartService chartService;

    @RabbitListener(queues = {BI_DLX_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if(message == null){
                channel.basicNack(deliveryTag,false,false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息异常");
            }
            Long cahrtId = Long.valueOf(message);
            Chart chart = chartService.getById(cahrtId);
            if(chart == null){
                channel.basicNack(deliveryTag,false,false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表不存在");
            }
            chart.setStatus("failed");
            boolean b = chartService.updateById(chart);
            //todo 用SSE推送给前端
            if(!b){
                channel.basicNack(deliveryTag,false,false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图表状态失败");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }
    }
}

