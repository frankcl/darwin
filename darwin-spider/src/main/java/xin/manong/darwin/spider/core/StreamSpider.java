package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.M3U8Input;
import xin.manong.darwin.spider.output.ByteArrayOutput;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;

/**
 * 流媒体爬虫
 * 1. M3U8视频流
 *
 * @author frankcl
 * @date 2023-03-24 16:23:28
 */
@Component
public class StreamSpider extends Spider {

    private static final String CATEGORY = "stream";
    private static final String LIVE_STREAM_FINISH_TAG = "#EXT-X-ENDLIST";

    @Resource
    protected SpiderConfig config;
    @Resource
    protected ProxyService proxyService;

    public StreamSpider() {
        super(CATEGORY);
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Input input = buildInput(record, context);
        if (input instanceof HTTPInput) {
            checkLiveStream((HTTPInput) input, record);
            Proxy proxy = record.isUseProxy() ? proxyService.randomGet(record.fetchMethod) : null;
            input = new M3U8Input(record, proxy, config);
            record.mimeType = Spider.MIME_TYPE_VIDEO;
            record.subMimeType = Spider.SUB_MIME_TYPE_MP4;
            record.httpCode = Spider.HTTP_CODE_OK;
        }
        write(record, input, context);
    }

    /**
     * 检测是否为直播流
     * 如果为直播流抛出异常
     *
     * @param input HTTP输入源
     * @param record URL数据
     * @throws IOException I/O异常
     */
    private void checkLiveStream(HTTPInput input, URLRecord record) throws IOException {
        try (input; ByteArrayOutput output = new ByteArrayOutput()) {
            input.open();
            input.transport(output);
            byte[] byteArray = output.getBytes();
            String charset = speculateCharset(byteArray, record);
            String content = new String(byteArray, charset);
            if (!content.contains(LIVE_STREAM_FINISH_TAG)) {
                record.httpCode = HTTP_CODE_NOT_ACCEPTABLE;
                throw new IOException(String.format("unsupported live stream url: %s", record.url));
            }
        }
    }
}
