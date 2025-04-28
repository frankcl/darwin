package xin.manong.darwin.spider.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.http.HttpProxy;

import java.net.*;
import java.util.Collections;
import java.util.List;

/**
 * 爬虫代理IP选择器
 *
 * @author frankcl
 * @date 2023-12-11 11:33:15
 */
public class SpiderProxySelector extends AbstractProxySelector {

    private static final Logger logger = LoggerFactory.getLogger(SpiderProxySelector.class);

    private final int category;
    private final ProxyService proxyService;

    public SpiderProxySelector(int category, ProxyService proxyService) {
        this.category = category;
        this.proxyService = proxyService;
    }

    @Override
    public List<Proxy> select(URI uri) {
        HttpProxy httpProxy = buildHttpProxy(proxyService.randomGet(category));
        if (httpProxy != null) logger.info("Fetch URI:{} for using proxy:{}",
                uri.toString(), httpProxy.address().toString());
        return Collections.singletonList(httpProxy == null ? Proxy.NO_PROXY : httpProxy);
    }
}
