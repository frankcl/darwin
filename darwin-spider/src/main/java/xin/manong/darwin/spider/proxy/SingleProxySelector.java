package xin.manong.darwin.spider.proxy;

import xin.manong.weapon.base.http.HttpProxy;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

/**
 * 单一代理选择器
 *
 * @author frankcl
 * @date 2025-04-18 20:23:51
 */
public class SingleProxySelector extends AbstractProxySelector {

    private final xin.manong.darwin.common.model.Proxy proxy;

    public SingleProxySelector(xin.manong.darwin.common.model.Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public List<Proxy> select(URI uri) {
        HttpProxy httpProxy = buildHttpProxy(proxy);
        return Collections.singletonList(httpProxy == null ? Proxy.NO_PROXY : httpProxy);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

    }
}
