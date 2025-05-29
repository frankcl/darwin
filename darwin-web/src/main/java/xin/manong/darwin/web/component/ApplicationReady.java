package xin.manong.darwin.web.component;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xin.manong.darwin.runner.core.Allocator;
import xin.manong.darwin.runner.core.DashboardRunner;
import xin.manong.darwin.runner.core.PlanRunner;
import xin.manong.darwin.runner.manage.ExecuteRunnerRegistry;
import xin.manong.darwin.runner.monitor.ConcurrencyQueueMonitor;
import xin.manong.darwin.runner.monitor.ExpiredCleaner;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用启动后处理
 * 启动执行器
 *
 * @author frankcl
 * @date 2025-05-28 15:06:31
 */
@Component
public class ApplicationReady implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationReady.class);

    private final List<String> runnerIds;
    @Resource
    private ExecuteRunnerRegistry registry;

    public ApplicationReady() {
        runnerIds = new ArrayList<>();
        runnerIds.add(Allocator.ID);
        runnerIds.add(PlanRunner.ID);
        runnerIds.add(DashboardRunner.ID);
        runnerIds.add(ConcurrencyQueueMonitor.ID);
        runnerIds.add(ExpiredCleaner.ID);
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        logger.info("After application ready, bootstrap execute runners");
        for (String runnerId : runnerIds) registry.start(runnerId);
    }
}
