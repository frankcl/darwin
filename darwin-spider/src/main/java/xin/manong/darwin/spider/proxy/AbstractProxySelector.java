package xin.manong.darwin.spider.proxy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.http.HttpProxy;

import java.io.IOException;
import java.net.*;

/**
 * 代理选择器抽象定义
 *
 * @author frankcl
 * @date 2025-04-18 20:29:03
 */
public abstract class AbstractProxySelector extends ProxySelector {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractProxySelector.class);

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        logger.warn("Connect URL:{} failed", uri.toString());
    }

    /**
     * 构建HTTP代理
     *
     * @param proxy 代理数据
     * @return HTTP代理
     */
    protected HttpProxy buildHttpProxy(xin.manong.darwin.common.model.Proxy proxy) {
        if (proxy == null) return null;
        InetSocketAddress address = new InetSocketAddress(proxy.address, proxy.port);
        if (StringUtils.isEmpty(proxy.username) || StringUtils.isEmpty(proxy.password)) {
            return new HttpProxy(Proxy.Type.HTTP, address);
        }
        return new HttpProxy(Proxy.Type.HTTP, address, proxy.username, proxy.password);
    }
}
