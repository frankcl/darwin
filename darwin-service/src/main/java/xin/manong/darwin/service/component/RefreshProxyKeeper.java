package xin.manong.darwin.service.component;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.executor.ExecuteRunner;

/**
 * 代理保鲜
 *
 * @author frankcl
 * @date 2023-12-13 17:05:13
 */
public class RefreshProxyKeeper extends ExecuteRunner {

    private final ProxyService proxyService;

    public RefreshProxyKeeper(ProxyService proxyService, long refreshProxyTimeIntervalMs) {
        super("RefreshProxyKeeper", refreshProxyTimeIntervalMs);
        this.proxyService = proxyService;
    }

    @Override
    public void execute() throws Exception {
        for (int proxyCategory : Constants.SUPPORT_PROXY_CATEGORIES.keySet()) {
            proxyService.refreshCache(proxyCategory);
        }
    }
}
