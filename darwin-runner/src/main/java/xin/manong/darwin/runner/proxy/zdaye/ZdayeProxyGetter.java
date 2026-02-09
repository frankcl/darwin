package xin.manong.darwin.runner.proxy.zdaye;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.runner.proxy.ProxyGetConfig;
import xin.manong.darwin.runner.proxy.ProxyGetter;
import xin.manong.weapon.base.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 站大爷短效代理获取
 *
 * @author frankcl
 * @date 2026-02-09 13:57:27
 */
public class ZdayeProxyGetter extends ProxyGetter {

    private static final Logger logger = LoggerFactory.getLogger(ZdayeProxyGetter.class);

    private ProxyGetConfig config;
    private String proxyURL;

    @Override
    public boolean init(ProxyGetConfig config) {
        if (config == null || !config.check()) {
            logger.error("Zdaye proxy get config is invalid");
            return false;
        }
        proxyURL = String.format("%s/ShortProxy/GetIP/", config.baseURL);
        proxyURL = String.format("%s?api=%s&akey=%s&count=%d&timespan=3&type=3", proxyURL,
                config.appId, config.appSecret, config.batchSize);
        this.config = config;
        return true;
    }

    @Override
    public List<Proxy> batchGet() {
        try {
            HttpRequest request = HttpRequest.buildGetRequest(proxyURL, null);
            String content = executeHTTPRequest(request);
            ZdayeResponse<ZdayeProxyList> response = JSON.parseObject(content, new TypeReference<>() {
            });
            if (response.code != 10001) {
                logger.error("Zdaye get proxy time failed, message:{}", response.message);
                return List.of();
            }
            List<Proxy> proxies = new ArrayList<>();
            for (ZdayeProxy proxy : response.data.proxyList) {
                proxies.add(buildProxy(proxy));
            }
            return proxies;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 构建代理
     *
     * @param zdayeProxy 站大爷代理
     * @return 代理
     */
    private Proxy buildProxy(ZdayeProxy zdayeProxy) {
        Proxy proxy = new Proxy();
        proxy.address = zdayeProxy.ip;
        proxy.port = zdayeProxy.port;
        proxy.username = config.username;
        proxy.password = config.password;
        proxy.category = Constants.PROXY_CATEGORY_SHORT;
        proxy.expiredTime = System.currentTimeMillis() + zdayeProxy.timeout * 1000L;
        return proxy;
    }
}
