package xin.manong.darwin.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.service.iface.ProxyService;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

/**
 * 爬虫代理IP选择器
 *
 * @author frankcl
 * @date 2023-12-11 11:33:15
 */
@Component
public class SpiderProxySelector extends ProxySelector {

    private static final Logger logger = LoggerFactory.getLogger(SpiderProxySelector.class);

    @Resource
    protected ProxyService proxyService;

    @Override
    public List<Proxy> select(URI uri) {
        return null;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

    }
}
