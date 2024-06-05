package com.zm.bi.service.impl;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadMonitoringServiceImpl {

    private static final double CPU_LOAD_THRESHOLD = 0.75; // 设置CPU负载的阈值

    public boolean isSystemOverloaded() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad();
        return cpuLoad > CPU_LOAD_THRESHOLD;
    }
}