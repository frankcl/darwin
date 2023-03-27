package xin.manong.darwin.spider.function;

import org.springframework.stereotype.Component;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * 流媒体爬虫
 * 1. M3U8视频流
 *
 * @author frankcl
 * @date 2023-03-24 16:23:28
 */
@Component
public class StreamSpider extends Spider {

    public StreamSpider() {
        super("stream");
    }

    @Override
    public FetchResponse fetch(URLRecord record) {
        return null;
    }

    @Override
    public ParseResponse parse(URLRecord record, String html) {
        throw new UnsupportedOperationException("unsupported parse operation");
    }
}
