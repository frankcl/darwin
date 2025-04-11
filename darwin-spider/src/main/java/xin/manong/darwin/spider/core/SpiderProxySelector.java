package xin.manong.darwin.spider.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.http.HttpProxy;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

/**
 * 爬虫代理IP选择器
 *
 * @author frankcl
 * @date 2023-12-11 11:33:15
 */
public class SpiderProxySelector extends ProxySelector {

    private static final Logger logger = LoggerFactory.getLogger(SpiderProxySelector.class);

    private final int category;
    protected ProxyService proxyService;

    public SpiderProxySelector(int category, ProxyService proxyService) {
        this.category = category;
        this.proxyService = proxyService;
    }

    @Override
    public List<Proxy> select(URI uri) {
        HttpProxy httpProxy = build(proxyService.randomGet(category));
        if (httpProxy != null) logger.info("fetch URI[{}] for using proxy[{}]",
                uri.toString(), httpProxy.address().toString());
        return Collections.singletonList(httpProxy == null ? Proxy.NO_PROXY : httpProxy);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        logger.warn("connect URL[{}] failed", uri.toString());
    }

    /**
     * 构建HTTP代理
     *
     * @param proxy 代理数据
     * @return HTTP代理
     */
    private HttpProxy build(xin.manong.darwin.common.model.Proxy proxy) {
        if (proxy == null) return null;
        return StringUtils.isEmpty(proxy.username) || StringUtils.isEmpty(proxy.password) ?
                new HttpProxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.address, proxy.port)) :
                new HttpProxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.address, proxy.port),
                        proxy.username, proxy.password);
    }
}
