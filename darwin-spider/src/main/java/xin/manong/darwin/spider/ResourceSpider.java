package xin.manong.darwin.spider;

import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;

import java.io.InputStream;

/**
 * 资源爬虫
 * 1. 文档
 * 2. 图片
 * 3. 视频
 *
 * @author frankcl
 * @date 2023-03-24 16:22:15
 */
@Component
public class ResourceSpider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(ResourceSpider.class);

    public ResourceSpider() {
        super("resource");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Response httpResponse = null;
        InputStream inputStream = (InputStream) context.get(Constants.DARWIN_INPUT_STREAM);
        try {
            if (inputStream == null) {
                httpResponse = fetch(record, context);
                if (httpResponse == null) return;
                String suffix = getResourceSuffix(httpResponse);
                if (!StringUtils.isEmpty(suffix)) context.put(Constants.RESOURCE_SUFFIX, suffix);
                inputStream = httpResponse.body().byteStream();
            }
            if (inputStream == null || !writeContent(record, inputStream, context)) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("write fetch content failed for url[{}]", record.url);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取内容写入OSS失败");
            }
        } finally {
            if (inputStream != null) inputStream.close();
            if (httpResponse != null) httpResponse.close();
        }
    }
}
