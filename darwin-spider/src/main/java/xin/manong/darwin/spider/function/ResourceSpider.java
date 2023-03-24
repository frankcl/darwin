package xin.manong.darwin.spider.function;

import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * 资源爬虫
 * 1. 文档
 * 2. 图片
 * 3. 视频
 *
 * @author frankcl
 * @date 2023-03-24 16:22:15
 */
public class ResourceSpider extends Spider {
    @Override
    public FetchResponse fetch(URLRecord record) {
        return null;
    }

    @Override
    public ParseResponse parse(URLRecord record, String html) {
        throw new UnsupportedOperationException("unsupported parse operation");
    }
}
