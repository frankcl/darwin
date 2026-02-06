package xin.manong.darwin.service.component;

import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.watch.WatchEvent;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.etcd.EtcdClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 短效代理更新监听器
 *
 * @author frankcl
 * @date 2026-02-06 16:42:00
 */
@Component
public class ProxyUpdateListener implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ProxyUpdateListener.class);

    @Resource
    private EtcdClient etcdClient;
    @Resource
    private ProxyService proxyService;

    /**
     * 处理代理更新
     *
     * @param updateTime 更新时间
     */
    public void onUpdate(long updateTime) {
        if (System.currentTimeMillis() - updateTime > 120000L) {
            logger.warn("Update time delay, ignore it");
            return;
        }
        proxyService.refreshCache(Constants.PROXY_CATEGORY_SHORT);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        etcdClient.addWatch(Constants.ETCD_KEY_PROXY_UPDATE, watchResponse -> {
            List<WatchEvent> watchEvents = watchResponse.getEvents();
            for (WatchEvent watchEvent : watchEvents) {
                try {
                    KeyValue keyValue = watchEvent.getKeyValue();
                    WatchEvent.EventType eventType = watchEvent.getEventType();
                    if (Objects.requireNonNull(eventType) == WatchEvent.EventType.PUT) {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        onUpdate(Long.parseLong(value));
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        etcdClient.removeWatch(Constants.ETCD_KEY_PROXY_UPDATE);
    }
}
