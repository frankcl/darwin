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

    @Resource
    private ExecuteRunnerRegistry registry;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        logger.info("After application ready, bootstrap execute runners");
        registry.start(Allocator.ID);
        registry.start(PlanRunner.ID);
        registry.start(DashboardRunner.ID);
        registry.start(ConcurrencyQueueMonitor.ID);
    }
}
