package xin.manong.darwin.web.config;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.service.component.RecordDispatcher;
import xin.manong.darwin.service.iface.URLService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * web应用配置
 *
 * @author frankcl
 * @date 2022-08-24 13:04:15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.web")
public class WebConfig {

    public boolean ignoreCheckPermission = false;
    public String name;

    /**
     * 构建数据分发器
     *
     * @param urlService URL服务
     * @return 数据分发器
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RecordDispatcher buildRecordDispatcher(URLService urlService) {
        return new RecordDispatcher(urlService);
    }

    /**
     * 构建公共线程池
     *
     * @return 公共线程池
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService buildCommonThreadPool() {
        return Executors.newFixedThreadPool(5, new ThreadFactory() {

            private final AtomicInteger n = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, String.format("CommonThread-%d", n.getAndIncrement()));
            }
        });
    }
}
