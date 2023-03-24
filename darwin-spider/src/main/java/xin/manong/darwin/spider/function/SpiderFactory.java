package xin.manong.darwin.spider.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫工厂
 *
 * @author frankcl
 * @date 2023-03-24 16:26:47
 */
public class SpiderFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpiderFactory.class);

    private static final Map<Class, Spider> SPIDER_MAP = initSpiderMap();

    /**
     * 初始化爬虫实例
     *
     * @return 爬虫实例Map
     */
    private static Map<Class, Spider> initSpiderMap() {
        Map<Class, Spider> spiderMap = new HashMap<>();
        spiderMap.put(HTMLSpider.class, new HTMLSpider());
        spiderMap.put(ResourceSpider.class, new ResourceSpider());
        spiderMap.put(StreamSpider.class, new StreamSpider());
        return spiderMap;
    }

    /**
     * 根据URL记录构建爬虫实例
     *
     * @param record URL记录
     * @return 爬虫实例
     */
    public static Spider build(URLRecord record) {
        if (record == null) {
            logger.error("url record is null");
            throw new RuntimeException("URL记录为空");
        }
        if (record.category == null || record.category == Constants.CONTENT_CATEGORY_TEXT ||
            record.category == Constants.CONTENT_CATEGORY_LIST) {
            return SPIDER_MAP.get(HTMLSpider.class);
        } else if (record.category == Constants.CONTENT_CATEGORY_RESOURCE) {
            return SPIDER_MAP.get(ResourceSpider.class);
        } else if (record.category == Constants.CONTENT_CATEGORY_STREAM) {
            return SPIDER_MAP.get(StreamSpider.class);
        }
        logger.error("unsupported spider for category[{}]", record.category);
        throw new RuntimeException(String.format("根据URL类型[%d]未找到对应爬虫实例", record.category));
    }
}
