package xin.manong.darwin.runner.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.weapon.base.util.ReflectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理获取注册
 *
 * @author frankcl
 * @date 2026-02-09 09:47:38
 */
public class ProxyGetterRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ProxyGetterRegistry.class);

    private List<ProxyGetter> proxyGetters;

    /**
     * 初始化
     *
     * @param configs 配置列表
     */
    public boolean init(List<ProxyGetConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            logger.error("Proxy getter configs are empty");
            return false;
        }
        proxyGetters = new ArrayList<>();
        for (ProxyGetConfig config : configs) {
            if (!config.check()) {
                logger.error("Proxy getter config is invalid");
                return false;
            }
            try {
                ProxyGetter proxyGetter = (ProxyGetter) ReflectUtil.newInstance(config.className, null);
                if (!proxyGetter.init(config)) {
                    logger.error("Init proxy getter failed for {}", config.className);
                    return false;
                }
                proxyGetters.add(proxyGetter);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (proxyGetters == null) return;
        for (ProxyGetter proxyGetter : proxyGetters) {
            proxyGetter.destroy();
        }
    }

    /**
     * 批量获取代理
     *
     * @return 代理列表
     */
    public List<Proxy> batchGet() {
        List<Proxy> proxies = new ArrayList<>();
        if (proxyGetters == null) return proxies;
        for (ProxyGetter proxyGetter : proxyGetters) {
            proxies.addAll(proxyGetter.batchGet());
        }
        return proxies;
    }
}
