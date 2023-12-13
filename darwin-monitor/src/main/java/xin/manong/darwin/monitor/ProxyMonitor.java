package xin.manong.darwin.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.iface.ProxyService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 代理监控
 * 1. 短效代理填充
 * 2. 无效代理清理
 *
 * @author frankcl
 * @date 2023-12-13 11:46:59
 */
public class ProxyMonitor extends ExecuteMonitor {

    private static final Logger logger = LoggerFactory.getLogger(ProxyMonitor.class);

    @Resource
    protected ProxyService proxyService;

    public ProxyMonitor(long checkTimeIntervalMs) {
        super(checkTimeIntervalMs);
    }

    @Override
    public void execute() {
        int sweepCount = proxyService.deleteExpired();
        logger.info("sweep expired proxy count[{}]", sweepCount);
        List<Proxy> newProxies = batchGet();
        Long addCount = 0L;
        for (Proxy newProxy : newProxies) {
            if (!newProxy.check() || newProxy.category != Constants.PROXY_CATEGORY_SHORT) continue;
            try {
                Proxy proxy = proxyService.get(newProxy.address, newProxy.port);
                if (proxy != null) proxyService.delete(proxy.id);
                proxyService.add(newProxy);
                addCount++;
            } catch (Exception e) {
                logger.error("add new short proxy failed");
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("batch get new proxy count[{}], add success count[{}]", newProxies.size(), addCount);
    }

    /**
     * 批量获取短效代理
     *
     * @return 短效代理列表
     */
    private List<Proxy> batchGet() {
        //TODO 调用服务获取新的短效代理
        return new ArrayList<>();
    }
}
