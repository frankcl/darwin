package xin.manong.darwin.runner.proxy;

import com.alibaba.fastjson.JSONObject;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 积流代理获取器
 *
 * @author frankcl
 * @date 2026-02-06 14:37:50
 */
public class JiliuProxyGetter implements ProxyGetter {

    private static final Logger logger = LoggerFactory.getLogger(JiliuProxyGetter.class);

    private static final String RESPONSE_KEY_CODE = "code";
    private static final String RESPONSE_KEY_MESSAGE = "msg";
    private static final String RESPONSE_KEY_DATA = "data";

    private String proxyURL;
    private String proxyTimeURL;
    private HttpClient httpClient;
    private ProxyGetConfig config;

    @Override
    public boolean init(ProxyGetConfig config) {
        if (config == null || !config.check()) {
            logger.error("Jiliu proxy get config is invalid");
            return false;
        }
        proxyURL = String.format("%s/getdip", config.baseURL);
        proxyTimeURL = String.format("%s/getiptime", config.baseURL);
        proxyURL = String.format("%s?app_id=%s&app_secret=%s&num=%d", proxyURL,
                config.appId, config.appSecret, config.batchSize);
        proxyTimeURL = String.format("%s?app_id=%s&app_secret=%s", proxyTimeURL,
                config.appId, config.appSecret);
        httpClient = new HttpClient();
        this.config = config;
        return true;
    }

    @Override
    public List<Proxy> batchGet() {
        HttpRequest request = HttpRequest.buildGetRequest(proxyURL, null);
        String content = executeHTTPRequest(request);
        if (StringUtils.isEmpty(content)) return List.of();
        content = content.replace("\n", ",");
        request = HttpRequest.buildGetRequest(proxyTimeURL + "&proxy=" + content, null);
        long startTime = System.currentTimeMillis();
        content = executeHTTPRequest(request);
        try {
            JSONObject jsonResponse = JSONObject.parseObject(content);
            if (jsonResponse == null) {
                logger.error("Invalid response:{} for {}", content, proxyTimeURL);
                return List.of();
            }
            Integer code = jsonResponse.getInteger(RESPONSE_KEY_CODE);
            if (code == null || code != 0) {
                logger.error("Get proxy time failed, message:{}", jsonResponse.getString(RESPONSE_KEY_MESSAGE));
                return List.of();
            }
            List<Proxy> proxies = new ArrayList<>();
            Map<String, Object> proxyTimeMap = jsonResponse.getJSONObject(RESPONSE_KEY_DATA);
            for (Map.Entry<String, Object> entry : proxyTimeMap.entrySet()) {
                long expiredTime = startTime + (int) entry.getValue() * 1000L;
                proxies.add(buildProxy(entry.getKey(), expiredTime));
            }
            return proxies;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param request HTTP请求
     * @return 成功返回响应文本，否则返回null
     */
    private String executeHTTPRequest(HttpRequest request) {
        try (Response response = httpClient.execute(request)) {
            if (!response.isSuccessful()) {
                logger.error("Execute proxy request failed, http code:{} for {}",
                        response.code(), request.requestURL);
                return null;
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                logger.error("Execute proxy request failed, response body is null for {}", request.requestURL);
                return null;
            }
            String content = responseBody.string();
            if (StringUtils.isEmpty(content)) {
                logger.error("Execute proxy request failed, response body is empty for {}", request.requestURL);
                return null;
            }
            return content;
        } catch (Exception e) {
            logger.error("Execute proxy request exception for {}", request.requestURL);
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据文本构建代理
     *
     * @param content 文本
     * @return 代理
     */
    private Proxy buildProxy(String content, long expiredIntervalMs) {
        content = content.trim();
        int pos = content.lastIndexOf(":");
        Proxy proxy = new Proxy();
        proxy.address = pos == -1 ? content : content.substring(0, pos);
        proxy.username = config.username;
        proxy.password = config.password;
        proxy.category = Constants.PROXY_CATEGORY_SHORT;
        proxy.expiredTime = expiredIntervalMs;
        if (pos != -1) proxy.port = Integer.parseInt(content.substring(pos + 1));
        return proxy;
    }
}
