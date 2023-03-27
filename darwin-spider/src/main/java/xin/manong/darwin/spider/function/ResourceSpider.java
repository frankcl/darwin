package xin.manong.darwin.spider.function;

import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestMethod;

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
    public FetchResponse fetch(URLRecord record) {
        Response httpResponse = null;
        try {
            HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).method(RequestMethod.GET).build();
            if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers = record.headers;
            httpResponse = httpClient.execute(httpRequest);
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("execute http request failed for url[{}]", record.url);
                return FetchResponse.buildError(httpResponse == null ? 500 :
                        httpResponse.code(), "执行HTTP请求失败");
            }
            if (!writeContent(record, httpResponse.body().byteStream())) {
                record.status = Constants.URL_STATUS_FAIL;
                logger.error("write fetch content failed for url[{}]", record.url);
                return FetchResponse.buildError(httpResponse.code(), "抓取内容写入OSS失败");
            }
            return FetchResponse.buildOK(httpResponse.code(), null);
        } catch (Exception e) {
            if (httpResponse != null) httpResponse.close();
            record.status = Constants.URL_STATUS_FAIL;
            logger.error("fetch content error for url[{}]", record.url);
            logger.error(e.getMessage(), e);
            return FetchResponse.buildError(httpResponse == null ? 500 : httpResponse.code(), e.getMessage());
        }
    }

    @Override
    public ParseResponse parse(URLRecord record, String html) {
        throw new UnsupportedOperationException("unsupported parse operation");
    }
}
