package xin.manong.darwin.executor.scheduler;

import jakarta.annotation.Resource;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.executor.ExecuteRunner;

/**
 * 代理保鲜
 *
 * @author frankcl
 * @date 2023-12-13 17:05:13
 */
public class ProxyRefresher extends ExecuteRunner {

    public static final String NAME = "ProxyRefresher";
    @Resource
    private ProxyService proxyService;

    public ProxyRefresher(long refreshProxyTimeIntervalMs) {
        super(NAME, refreshProxyTimeIntervalMs);
    }

    @Override
    public void execute() throws Exception {
        for (int proxyCategory : Constants.SUPPORT_PROXY_CATEGORIES.keySet()) {
            proxyService.refreshCache(proxyCategory);
        }
    }
}
