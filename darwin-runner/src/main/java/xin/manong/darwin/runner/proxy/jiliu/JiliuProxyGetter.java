package xin.manong.darwin.runner.proxy.jiliu;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.runner.proxy.ProxyGetConfig;
import xin.manong.darwin.runner.proxy.ProxyGetter;
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
public class JiliuProxyGetter extends ProxyGetter {

    private static final Logger logger = LoggerFactory.getLogger(JiliuProxyGetter.class);

    private String proxyURL;
    private String proxyTimeURL;
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
            JiliuResponse<Map<String, Integer>> response = JSONObject.parseObject(
                    content, new TypeReference<>() {});
            if (response == null) {
                logger.error("Invalid response:{} for {}", content, proxyTimeURL);
                return List.of();
            }
            if (response.code != 0) {
                logger.error("Get proxy time failed, message:{}", response.message);
                return List.of();
            }
            List<Proxy> proxies = new ArrayList<>();
            Map<String, Integer> proxyTimeMap = response.data;
            for (Map.Entry<String, Integer> entry : proxyTimeMap.entrySet()) {
                long expiredTime = startTime + entry.getValue() * 1000L;
                proxies.add(buildProxy(entry.getKey(), expiredTime));
            }
            return proxies;
        } catch (Exception e) {
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
