package xin.manong.darwin.spider.function;

import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-24 16:18:17
 */
public abstract class Spider {

    /**
     * 抓取
     *
     * @param record URL记录
     * @return 抓取响应
     */
    abstract FetchResponse fetch(URLRecord record);

    /**
     * 解析
     *
     * @param record URL记录
     * @param html HTML
     * @return 解析响应
     */
    abstract ParseResponse parse(URLRecord record, String html);
}
