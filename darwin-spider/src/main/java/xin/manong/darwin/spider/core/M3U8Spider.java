package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import xin.manong.darwin.common.Constants;
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
public class M3U8Spider extends Spider {

    private static final int HTTP_CODE_OK = 200;
    private static final String MIME_TYPE_VIDEO = "video";
    private static final String SUB_MIME_TYPE_MP4 = "mp4";

    @Resource
    private ProxyService proxyService;

    @Override
    public MediaType handle(URLRecord record, Input input, Context context) throws IOException {
        record.category = Constants.CONTENT_CATEGORY_STREAM;
        Proxy proxy = record.isUseProxy() ? proxyService.randomGet(record.fetchMethod) : null;
        try (M3U8Input m3U8Input = new M3U8Input(record, proxy, spiderConfig)) {
            record.mimeType = MIME_TYPE_VIDEO;
            record.subMimeType = SUB_MIME_TYPE_MP4;
            record.mediaType = MediaType.VIDEO.name();
            record.httpCode = HTTP_CODE_OK;
            writer.write(record, m3U8Input, context);
            return MediaType.UNKNOWN;
        }
    }

    @Override
    public List<MediaType> supportedMediaTypes() {
        return List.of(MediaType.M3U8);
    }
}
