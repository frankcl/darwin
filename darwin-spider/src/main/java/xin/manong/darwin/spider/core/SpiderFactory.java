package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

/**
 * 爬虫工厂
 *
 * @author frankcl
 * @date 2023-03-24 16:26:47
 */
@Component
public class SpiderFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpiderFactory.class);

    @Resource
    protected HTMLSpider htmlSpider;
    @Resource
    protected ResourceSpider resourceSpider;
    @Resource
    protected StreamSpider streamSpider;

    /**
     * 根据URL记录构建爬虫实例
     *
     * @param record URL记录
     * @return 爬虫实例
     */
    public Spider build(@NotNull URLRecord record) {
        if (record.category == null || record.category == Constants.CONTENT_CATEGORY_CONTENT ||
            record.category == Constants.CONTENT_CATEGORY_LIST) {
            return htmlSpider;
        } else if (record.category == Constants.CONTENT_CATEGORY_RESOURCE) {
            return resourceSpider;
        } else if (record.category == Constants.CONTENT_CATEGORY_STREAM) {
            return streamSpider;
        }
        logger.error("Unsupported spider for category:{}", record.category);
        throw new IllegalArgumentException("爬虫类型不支持");
    }
}
