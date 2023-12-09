package xin.manong.darwin.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务层配置
 *
 * @author frankcl
 * @date 2022-08-24 13:04:15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.service")
public class ServiceConfig {

    public String jobTable;
    public String jobIndexName;
    public String urlTable;
    public String urlIndexName;
    public String jobTopic;
    public String recordTopic;
}
