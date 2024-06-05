package com.zm.bi.mapper;

import com.zm.bi.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
* @author 29524
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-06-02 20:37:47
* @Entity com.zm.bi.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String,Object>> queryChartData(String querySql);
}




