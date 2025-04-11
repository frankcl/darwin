package xin.manong.darwin.spider.receiver;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.spider.core.SpiderFactory;
import xin.manong.weapon.base.log.JSONLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * URL消息接收配置信息
 *
 * @author frankcl
 * @date 2025-04-10 14:48:53
 */
@Data
@Configuration
public class URLReceiverConfig {

    @Resource(name = "urlAspectLogger")
    protected JSONLogger aspectLogger;

    /**
     * 构建HTML URL接收器
     *
     * @param spiderFactory 爬虫工厂
     * @return HTML URL接收器
     */
    @Bean(name = "htmlURLReceiver")
    public URLReceiver buildHTMLURLReceiver(SpiderFactory spiderFactory) {
        Set<String> supportedCategory = new HashSet<>();
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_CONTENT));
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_LIST));
        return new URLReceiver(spiderFactory, aspectLogger, supportedCategory);
    }

    /**
     * 构建资源URL接收器
     *
     * @param spiderFactory 爬虫工厂
     * @return 资源URL接收器
     */
    @Bean(name = "resourceURLReceiver")
    public URLReceiver buildResourceURLReceiver(SpiderFactory spiderFactory) {
        Set<String> supportedCategory = new HashSet<>();
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_RESOURCE));
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_STREAM));
        return new URLReceiver(spiderFactory, aspectLogger, supportedCategory);
    }
}
