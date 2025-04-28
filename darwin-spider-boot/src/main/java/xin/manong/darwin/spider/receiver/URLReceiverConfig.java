package xin.manong.darwin.spider.receiver;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.spider.core.Router;

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

    /**
     * 构建网页数据接收器
     *
     * @param router 爬虫路由
     * @param aspectLogSupport 切面日志支持
     * @return 网页数据接收器
     */
    @Bean(name = "pageReceiver")
    public URLReceiver buildPageReceiver(Router router, AspectLogSupport aspectLogSupport) {
        Set<String> supportedCategory = new HashSet<>();
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_PAGE));
        return new URLReceiver(router, aspectLogSupport, supportedCategory);
    }

    /**
     * 构建资源数据接收器
     *
     * @param router 爬虫路由
     * @param aspectLogSupport 切面日志支持
     * @return 资源数据接收器
     */
    @Bean(name = "resourceReceiver")
    public URLReceiver buildResourceReceiver(Router router, AspectLogSupport aspectLogSupport) {
        Set<String> supportedCategory = new HashSet<>();
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_RESOURCE));
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_STREAM));
        return new URLReceiver(router, aspectLogSupport, supportedCategory);
    }
}
