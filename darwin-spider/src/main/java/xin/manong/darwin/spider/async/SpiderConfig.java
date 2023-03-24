package xin.manong.darwin.spider.async;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.base.log.JSONLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 爬虫配置信息
 *
 * @author frankcl
 * @date 2023-03-24 14:48:53
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.spider")
public class SpiderConfig {

    private static final int DEFAULT_RECORD_QUEUE_SIZE = 100;

    public int recordQueueSize = DEFAULT_RECORD_QUEUE_SIZE;
    public String aspectLogFile;

    @Bean(name = "textRecordQueue")
    public BlockingQueue<SpiderRecord> buildTextRecordQueue() {
        return new ArrayBlockingQueue<>(recordQueueSize);
    }

    @Bean(name = "resourceRecordQueue")
    public BlockingQueue<SpiderRecord> buildResourceRecordQueue() {
        return new ArrayBlockingQueue<>(recordQueueSize);
    }

    @Bean(name = "spiderAspectLogger")
    public JSONLogger spiderAspectLogger() {
        return new JSONLogger(aspectLogFile, null);
    }
}
