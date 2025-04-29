package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.M3U8Input;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.util.List;

/**
 * M3U8爬虫
 *
 * @author frankcl
 * @date 2025-04-27 21:17:23
 */
@Component
public class M3U8Spider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(M3U8Spider.class);
    private static final int HTTP_CODE_OK = 200;

    @Resource
    private ProxyService proxyService;

    @Override
    public MediaType handle(URLRecord record, Input input, Context context) throws IOException {
        record.category = Constants.CONTENT_CATEGORY_STREAM;
        Proxy proxy = record.isUseProxy() ? proxyService.randomGet(record.fetchMethod) : null;
        try (M3U8Input m3U8Input = new M3U8Input(record, proxy, spiderConfig)) {
            record.mediaType = MediaType.VIDEO_MP4;
            record.httpCode = HTTP_CODE_OK;
            writer.write(record, m3U8Input, context);
            return MediaType.UNKNOWN;
        }
    }

    @Override
    public List<MediaType> supportedMediaTypes() {
        return List.of(MediaType.STREAM_M3U8);
    }
}
