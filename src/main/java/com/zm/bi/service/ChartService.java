package com.zm.bi.service;

import com.zm.bi.common.BaseResponse;
import com.zm.bi.model.dto.chart.GenChartByAiRequest;
import com.zm.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zm.bi.model.vo.BiResponse;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 29524
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-06-02 20:37:47
*/
public interface ChartService extends IService<Chart> {

    public BaseResponse<BiResponse> genChartByAiAsyncMq(MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    public BaseResponse<BiResponse> genChartByAi(MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    public BaseResponse<BiResponse> genChartByAiAsync(MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);
}
