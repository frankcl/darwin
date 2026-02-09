package xin.manong.darwin.runner.monitor;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.runner.proxy.ProxyGetterRegistry;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.etcd.EtcdClient;
import xin.manong.weapon.base.event.ErrorEvent;
import xin.manong.weapon.base.executor.ExecuteRunner;

import java.util.List;

/**
 * 代理监控
 * 1. 短效代理填充
 * 2. 无效代理清理
 *
 * @author frankcl
 * @date 2023-12-13 11:46:59
 */
public class ProxyMonitor extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProxyMonitor.class);

    public static final String ID = "ProxyMonitor";

    @Resource
    private ProxyService proxyService;
    @Resource
    private EtcdClient etcdClient;
    @Autowired(required = false)
    private ProxyGetterRegistry proxyGetterRegistry;

    public ProxyMonitor(long executeTimeIntervalMs) {
        super(ID, executeTimeIntervalMs);
        this.setName("代理监控器");
        this.setDescription("负责定时刷新短效代理并更新代理缓存");
    }

    @Override
    public void execute() {
        int sweepCount = proxyService.deleteExpired();
        logger.info("Sweep expired proxy count:{}", sweepCount);
        if (proxyGetterRegistry == null) return;
        List<Proxy> shortProxies = proxyGetterRegistry.batchGet();
        long successCount = 0L;
        for (Proxy shortProxy : shortProxies) {
            if (!shortProxy.check() || shortProxy.category != Constants.PROXY_CATEGORY_SHORT) continue;
            Proxy proxy = proxyService.get(shortProxy.address, shortProxy.port);
            if (proxy != null) proxyService.delete(proxy.id);
            if (proxyService.add(shortProxy)) {
                logger.info("Add new short proxy:{} success", shortProxy);
                successCount++;
                continue;
            }
            notifyErrorEvent(new ErrorEvent("添加短效代理失败"));
        }
        if (successCount > 0 && etcdClient != null) {
            etcdClient.put(Constants.ETCD_KEY_PROXY_UPDATE, String.valueOf(System.currentTimeMillis()));
        }
        logger.info("Batch get short proxy count:{}, add success count:{}", shortProxies.size(), successCount);
    }
}
