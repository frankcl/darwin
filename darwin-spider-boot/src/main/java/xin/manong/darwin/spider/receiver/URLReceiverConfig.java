package xin.manong.darwin.spider.receiver;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.spider.core.Router;

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
     * 构建数据接收器
     *
     * @param router 爬虫路由
     * @param aspectLogSupport 切面日志支持
     * @return 网页数据接收器
     */
    @Bean(name = "URLReceiver")
    public URLReceiver buildURLReceiver(Router router, AspectLogSupport aspectLogSupport) {
        return new URLReceiver(router, aspectLogSupport);
    }
}
