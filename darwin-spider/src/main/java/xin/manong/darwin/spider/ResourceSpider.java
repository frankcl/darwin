package xin.manong.darwin.spider;

import okhttp3.Response;
import org.springframework.stereotype.Component;
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

    private static final String CATEGORY = "resource";

    public ResourceSpider() {
        super(CATEGORY);
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Response httpResponse = null;
        InputStream inputStream = getPrevInputStream(record, context);
        try {
            if (inputStream == null) {
                httpResponse = httpRequest(record);
                inputStream = getHTTPInputStream(httpResponse, record);
            }
            write(record, inputStream, context);
        } finally {
            if (inputStream != null) inputStream.close();
            if (httpResponse != null) httpResponse.close();
        }
    }
}
