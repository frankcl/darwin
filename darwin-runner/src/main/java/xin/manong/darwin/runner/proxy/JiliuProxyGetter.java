package xin.manong.darwin.runner.proxy;

import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;

import java.util.Arrays;
import java.util.List;

/**
 * 积流代理获取器
 *
 * @author frankcl
 * @date 2026-02-06 14:37:50
 */
public class JiliuProxyGetter implements ProxyGetter {

    private static final Logger logger = LoggerFactory.getLogger(JiliuProxyGetter.class);

    private static final String REQUEST_URL_FORMAT = "%s?app_id=%s&app_secret=%s&num=%d";

    private String requestURL;
    private HttpClient httpClient;
    private ProxyGetConfig config;

    @Override
    public boolean init(ProxyGetConfig config) {
        if (config == null || !config.check()) {
            logger.error("Jiliu proxy get config is invalid");
            return false;
        }
        requestURL = String.format(REQUEST_URL_FORMAT, config.apiURL,
                config.appId, config.appSecret, config.batchSize);
        httpClient = new HttpClient();
        this.config = config;
        return true;
    }

    @Override
    public List<Proxy> batchGet() {
        HttpRequest request = HttpRequest.buildGetRequest(requestURL, null);
        try (Response response = httpClient.execute(request)) {
            if (!response.isSuccessful()) {
                logger.error("Get proxy failed, http code:{}", response.code());
                return List.of();
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                logger.error("Get proxy failed, response body is null");
                return List.of();
            }
            String content = responseBody.string();
            if (StringUtils.isEmpty(content)) {
                logger.error("Get proxy failed, response body is empty");
                return List.of();
            }
            return Arrays.stream(content.split("\n")).
                    filter(ip -> StringUtils.isNotEmpty(ip.trim())).
                    map(this::buildProxy).toList();
        } catch (Exception e) {
            logger.error("Get proxy failed");
            logger.error(e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 根据文本构建代理
     *
     * @param content 文本
     * @return 代理
     */
    private Proxy buildProxy(String content) {
        content = content.trim();
        int pos = content.lastIndexOf(":");
        Proxy proxy = new Proxy();
        proxy.address = pos == -1 ? content : content.substring(0, pos);
        proxy.username = config.username;
        proxy.password = config.password;
        proxy.category = Constants.PROXY_CATEGORY_SHORT;
        proxy.expiredTime = System.currentTimeMillis() + config.expiredIntervalMs;
        if (pos != -1) proxy.port = Integer.parseInt(content.substring(pos + 1));
        return proxy;
    }
}
