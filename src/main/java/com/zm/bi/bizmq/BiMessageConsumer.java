package com.zm.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.zm.bi.common.ErrorCode;
import com.zm.bi.exception.BusinessException;
import com.zm.bi.manager.AiManager;
import com.zm.bi.manager.TaskManager;
import com.zm.bi.model.entity.Chart;
import com.zm.bi.service.ChartService;
import com.zm.bi.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

import static com.zm.bi.constant.RabbitMQConstant.BI_QUEUE_NAME;

@Component
@Slf4j
public class BiMessageConsumer {

    final String systemMessage = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "\n" +
            "分析需求：\n" +
            "\n" +
            "{数据分析的需求或者目标}\n" +
            "\n" +
            "原始数据：\n" +
            "\n" +
            "{csv格式的原始数据，用,作为分割符}\n" +
            "\n" +
            "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
            "\n" +
            "[[[[[\n" +
            "\n" +
            "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
            "\n" +
            "[[[[[\n" +
            "\n" +
            "{明确的数据分析结论、越详细越好，不要生成多余的注释}";

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @Resource
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private TaskManager taskManager;

    //指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        CompletableFuture<Void> future=CompletableFuture.runAsync(() -> {
            try {
                if(StringUtils.isBlank(message)){
                    channel.basicNack(deliveryTag,false,false);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
                }
                Long chartId = Long.valueOf(message);
                Chart chart = chartService.getById(chartId);
                if(chart == null){
                    channel.basicNack(deliveryTag,false,false);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表不存在");
                }
                //先修改图表任务状态为“执行中”。等执行成功后，修改为“已完成”，保存执行结果；执行失败后，状态修改为“失败”，记录任务失败信息
                Chart updateChrt = new Chart();
                updateChrt.setId(chart.getId());
                updateChrt.setStatus("running");
                boolean update = chartService.updateById(updateChrt);
                if (!update) {
                    channel.basicNack(deliveryTag,false,false);
                    handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                    return;
                }

                //调用AI
                String aiResult = aiManager.doSyncRequest(systemMessage, buildUserInput(chart),null);
                String[] splits = aiResult.split("\\[\\[\\[\\[\\[");
                if (splits.length < 3) {
                    channel.basicNack(deliveryTag,false,false);
                    handleChartUpdateError(chart.getId(), "AI生成错误");
                    return;
                }
                //trim去除字符串两端的空字符：包括空格和换行符等不可见字符
                String genChart = splits[1].trim();
                String genResult = splits[2].trim();

                Chart updateChartResult = new Chart();
                updateChartResult.setId(chart.getId());
                updateChartResult.setGenChart(genChart);
                updateChartResult.setGenResult(genResult);
                updateChartResult.setStatus("succeed");
                boolean result = chartService.updateById(updateChartResult);
                if (!result) {
                    channel.basicNack(deliveryTag,false,false);
                    handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
                }
                //todo 用SSE推送给前端
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);
        future.whenComplete((result,ex) -> {
            try {
                if(ex !=null || future.isCancelled()){
                    channel.basicNack(deliveryTag,false,false);
                }else{
                    channel.basicAck(deliveryTag,false);
                    taskManager.removeTask(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        taskManager.addTask(message,future);
    }


    /**
     * 构造用户输入
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart){
        String goal=chart.getGoal();
        String chartType=chart.getChartType();
        String csvData=chart.getCharData();
        //构造用户输入
        StringBuilder userInput=new StringBuilder();
        userInput.append("分析需求:").append("\n");

        //拼接分析目标
        String userGoal=goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal+=",请使用"+chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        //压缩后的数据
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }


    private void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult= new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean result = chartService.updateById(updateChartResult);
        if(!result){
            log.error("更新图表失败状态失败" + chartId + "," +execMessage);
        }
    }
}
