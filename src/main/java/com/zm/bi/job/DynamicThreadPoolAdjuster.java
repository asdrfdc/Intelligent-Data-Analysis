package com.zm.bi.job;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 动态调整线程池大小的类，根据CPU负载自动调整线程池的核心和最大线程数。
 */
@Service
public class DynamicThreadPoolAdjuster {

    /**
     * 需要调整的线程池执行器。
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * 获取操作系统的MXBean，用于获取CPU负载等系统信息。
     */
    private final OperatingSystemMXBean osBean;

    /**
     * 构造函数，注入ThreadPoolExecutor。
     *
     * @param threadPoolExecutor 要调整的线程池执行器
     */
    @Autowired
    public DynamicThreadPoolAdjuster(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * 定时任务，每隔10秒执行一次，用于检查并调整线程池大小。
     */
    @Scheduled(fixedRate = 10000)
    public void monitorThreadPool() {
        adjustThreadPoolSize();
    }

    /**
     * 根据CPU负载和线程池状态调整线程池的大小。
     * 此方法定期检查系统的CPU负载以及线程池的当前状态（核心线程数、最大线程数、当前线程数和活跃线程数）。
     * 根据预设的条件，动态调整线程池的大小，以优化系统资源的利用和性能。
     * 当CPU负载较高且线程池中活跃线程占总线程数的比例也较高时，增加线程池的大小。
     * 当CPU负载较低且线程池中活跃线程占总线程数的比例也较低时，减少线程池的大小。
     * 这种动态调整有助于在高负载下提高系统的处理能力，在低负载下减少资源浪费。
     */
    private void adjustThreadPoolSize() {
        // 获取系统当前的CPU负载
        double cpuLoad = osBean.getSystemCpuLoad();
        // 获取线程池的当前核心线程数、最大线程数、当前线程数和活跃线程数
        int currentCorePoolSize = threadPoolExecutor.getCorePoolSize();
        int currentMaxPoolSize = threadPoolExecutor.getMaximumPoolSize();
        int currentPoolSize = threadPoolExecutor.getPoolSize();
        int activeCount = threadPoolExecutor.getActiveCount();

        // 计算活跃线程占总线程数的比例
        // 获取活跃线程数和当前线程池大小的比例
        double activeRatio = (double) activeCount / currentPoolSize;

        // 当CPU负载高且线程池活跃度高，且核心线程数未达到上限时，增加核心线程数和最大线程数
        if (cpuLoad > 0.75 && activeRatio > 0.75 && currentCorePoolSize < 10) {
            threadPoolExecutor.setCorePoolSize(currentCorePoolSize + 1);
            threadPoolExecutor.setMaximumPoolSize(currentMaxPoolSize + 1);
        }
        // 当CPU负载低且线程池活跃度低，且核心线程数大于最小值时，减少核心线程数和最大线程数
        else if (cpuLoad < 0.25 && activeRatio < 0.25 && currentCorePoolSize > 2) {
            threadPoolExecutor.setCorePoolSize(currentCorePoolSize - 1);
            threadPoolExecutor.setMaximumPoolSize(currentMaxPoolSize - 1);
        }

        // 输出当前线程池的配置信息，用于调试和监控
        System.out.println("Adjusted ThreadPool Size: CorePoolSize=" + threadPoolExecutor.getCorePoolSize() +
                ", MaxPoolSize=" + threadPoolExecutor.getMaximumPoolSize() + ", PoolSize=" + currentPoolSize +
                ", ActiveCount=" + activeCount + ", CPU Load=" + cpuLoad + ", ActiveRatio=" + activeRatio);
    }
}

