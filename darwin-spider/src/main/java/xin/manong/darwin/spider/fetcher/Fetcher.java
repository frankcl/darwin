package xin.manong.darwin.spider.fetcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.CookieService;
import xin.manong.darwin.spider.core.SpiderConfig;
import xin.manong.weapon.base.util.CommonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 抓取器接口
 *
 * @author frankcl
 * @date 2026-04-22 17:00:19
 */
public abstract class Fetcher<T> {

    private static final Logger logger = LoggerFactory.getLogger(Fetcher.class);

    protected static final String HEADER_COOKIE = "Cookie";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_HOST = "Host";
    protected static final String HEADER_REFERER = "Referer";
    protected static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * 抓取数据
     *
     * @param record 数据
     * @return 响应
     */
    public abstract Response<T> fetch(URLRecord record) throws IOException;

    /**
     * 构建缺省headers
     *
     * @param config 爬虫配置
     * @param record 数据
     * @param cookieService cookie服务
     * @return 缺省headers
     */
    protected Map<String, String> buildDefaultHeaders(SpiderConfig config,
                                                      URLRecord record,
                                                      CookieService cookieService) {
        Map<String, String> headers = new HashMap<>();
        if (!StringUtils.isEmpty(config.userAgent)) headers.put(HEADER_USER_AGENT, config.userAgent);
        if (!StringUtils.isEmpty(record.parentURL)) {
            headers.put(HEADER_REFERER, CommonUtil.encodeURL(record.parentURL));
        }
        if (cookieService != null && record.systemCookie != null && record.systemCookie) {
            String cookie = cookieService.getCookie(record);
            if (StringUtils.isNotEmpty(cookie)) {
                logger.info("Set system cookie:{} for url:{}", cookie, record.url);
                headers.put(HEADER_COOKIE, cookie);
            }
        }
        String host = CommonUtil.getHost(record.url);
        if (!StringUtils.isEmpty(host) && !CommonUtil.isValidIP(host)) headers.put(HEADER_HOST, host);
        return headers;
    }
}
