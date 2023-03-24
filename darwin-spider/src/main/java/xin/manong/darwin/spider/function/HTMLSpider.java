package xin.manong.darwin.spider.function;

import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * 网页爬虫
 * 1. HTML网页
 * 2. JSON内容
 * 3. 其他文本内容
 *
 * @author frankcl
 * @date 2023-03-24 16:21:30
 */
public class HTMLSpider extends Spider {
    @Override
    public FetchResponse fetch(URLRecord record) {
        return null;
    }

    @Override
    public ParseResponse parse(URLRecord record, String html) {
        return null;
    }
}
