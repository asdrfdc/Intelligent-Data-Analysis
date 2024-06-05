package com.zm.bi.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.zhipu.oapi.ClientV4;
import com.zm.bi.bizmq.BiDelayMessageProducer;
import com.zm.bi.bizmq.BiMessageProducer;
import com.zm.bi.common.BaseResponse;
import com.zm.bi.common.ErrorCode;
import com.zm.bi.common.ResultUtils;
import com.zm.bi.controller.ChartController;
import com.zm.bi.exception.BusinessException;
import com.zm.bi.exception.ThrowUtils;
import com.zm.bi.manager.AiManager;
import com.zm.bi.manager.RedisLimiterManager;
import com.zm.bi.model.dto.chart.GenChartByAiRequest;
import com.zm.bi.model.entity.Chart;
import com.zm.bi.model.entity.User;
import com.zm.bi.model.vo.BiResponse;
import com.zm.bi.service.ChartService;
import com.zm.bi.mapper.ChartMapper;
import com.zm.bi.service.UserService;
import com.zm.bi.utils.ExcelUtils;
import com.zm.bi.utils.RetryUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
* @author 29524
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-06-02 20:37:47
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

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
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private BiDelayMessageProducer biDelayMessageProducer;

    @Resource
    private AiManager aiManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    public BaseResponse<BiResponse> genChartByAiAsyncMq(MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest,HttpServletRequest request){
        Result result= getResult(multipartFile, genChartByAiRequest, request);
        Chart chart = result.chart;

        //发送消息
        long newChartId=chart.getId();
        biMessageProducer.sendMessage(String.valueOf(newChartId));
        biDelayMessageProducer.sendMessage(String.valueOf(newChartId));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    public BaseResponse<BiResponse> genChartByAi(MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){
        Result result = getResult(multipartFile, genChartByAiRequest, request);
        Chart chart = result.chart;
        StringBuilder userInput = result.userInput;
        long modelId = result.modelId;

        Retryer<String> retryer= RetryUtil.createRetryer();

        // 调用AI并重试
        String aiResult = null;
        try {
            aiResult = retryer.call(() -> aiManager.doSyncRequest(systemMessage, userInput.toString(),null));

            upDateRunning(chart);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (RetryException e) {
            throw new RuntimeException(e);
        }

        genAll genAll = getGenAll(aiResult, result, chart);

        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genAll.genChart);
        biResponse.setChartId(chart.getId());
        biResponse.setGenResult(genAll.genResult);
        return ResultUtils.success(biResponse);
    }

    public BaseResponse<BiResponse> genChartByAiAsync( MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){
        Result result= getResult(multipartFile, genChartByAiRequest, request);
        long modelId = result.modelId;
        Chart chart = result.chart;
        StringBuilder userInput = result.userInput;

        Retryer<String> retryer = RetryUtil.createRetryer();
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 先修改图表任务状态为“执行中”。等执行成功后，修改为“已完成”，保存执行结果；执行失败后，状态修改为“失败”，记录任务失败信息
                    upDateRunning(chart);

                    // 调用AI并重试
                    String aiResult = retryer.call(() -> aiManager.doSyncRequest(systemMessage, userInput.toString(),null));

                    //整理AI生成结果保存到数据库
                    genAll genAll = getGenAll(aiResult, result, chart);
                } catch (Exception e) {
                    handleChartUpdateError(chart.getId(), "AI调用失败");
                }
            }, threadPoolExecutor);

            // 设置任务超时时间
            int timeoutSeconds = 10;
            future.orTimeout(timeoutSeconds, TimeUnit.SECONDS).get();

        } catch (RejectedExecutionException e) {
            // 线程池已满并且任务队列已满
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } catch (ExecutionException e) {
            // 执行过程中业务抛出异常或者超时异常
            handleChartUpdateError(chart.getId(), "执行超时！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } catch (InterruptedException e) {
            // 在执行过程中被外部中断
            handleChartUpdateError(chart.getId(), "执行因超时而被中断！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }






    private void upDateRunning(Chart chart) {
        // 先修改图表任务状态为“执行中”。等执行成功后，修改为“已完成”，保存执行结果；执行失败后，状态修改为“失败”，记录任务失败信息
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean update = this.updateById(updateChart);
        if (!update) {
            handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
        }
    }

    @NotNull
    private genAll getGenAll(String aiResult, Result result, Chart chart) {
        String[] splits = aiResult.split("\\[\\[\\[\\[\\[");
        if (splits.length < 3) {
            handleChartUpdateError(result.chart.getId(), "AI生成错误");
        }

        // trim去除字符串两端的空字符：包括空格和换行符等不可见字符
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        Chart updateChartResult = new Chart();
        updateChartResult.setId(result.chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("succeed");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        genAll genAll = new genAll(genChart, genResult);
        return genAll;
    }

    private static class genAll {
        public final String genChart;
        public final String genResult;

        public genAll(String genChart, String genResult) {
            this.genChart = genChart;
            this.genResult = genResult;
        }
    }

    @NotNull
    private Result getResult(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isNotBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标位空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称非法");

        // 校验文件
        String originalFilename = multipartFile.getOriginalFilename();
        long size = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1M");

        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);

        // 限流判断,每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());



        long modelId = 1L;

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setCharData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.PARAMS_ERROR, "图表保存失败");


        Result result = new Result(modelId, userInput, chart);
        return result;
    }

    private static class Result {
        public final long modelId;
        public final StringBuilder userInput;
        public final Chart chart;

        public Result(long modelId, StringBuilder userInput, Chart chart) {
            this.modelId = modelId;
            this.userInput = userInput;
            this.chart = chart;
        }

    }






    private void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult= new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean result = this.updateById(updateChartResult);
        if(!result){
            log.error("更新图表失败状态失败" + chartId + "," +execMessage);
        }
    }
}




