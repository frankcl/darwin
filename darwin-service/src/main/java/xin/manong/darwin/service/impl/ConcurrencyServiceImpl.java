package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import xin.manong.darwin.service.iface.ConcurrencyService;
import xin.manong.weapon.base.etcd.EtcdClient;

import java.util.Map;

/**
 * 并发连接服务实现
 *
 * @author frankcl
 * @date 2025-04-22 17:19:04
 */
@Service
public class ConcurrencyServiceImpl implements ConcurrencyService {

    private static final String DEFAULT_CONCURRENCY = "darwin/queue/defaultConcurrency";
    private static final String CONCURRENCY_CONNECTION_MAP = "darwin/queue/concurrencyConnectionMap";

    @Resource
    private EtcdClient etcdClient;

    @Override
    public int defaultConcurrency() {
        Integer v = etcdClient.get(DEFAULT_CONCURRENCY, Integer.class);
        return v == null ? 50 : v;
    }

    @Override
    public void defaultConcurrency(int concurrency) {
        if (concurrency <= 0) throw new IllegalArgumentException("缺失最大并发数必须大于0");
        if (!etcdClient.put(DEFAULT_CONCURRENCY, String.valueOf(concurrency))) {
            throw new RuntimeException("更新缺省最大并发数失败");
        }
    }

    @Override
    public Map<String, Integer> concurrencyConnectionMap() {
        Map<String, Integer> map = etcdClient.getMap(CONCURRENCY_CONNECTION_MAP, String.class, Integer.class);
        return map == null ? Map.of() : map;
    }

    @Override
    public void concurrencyConnectionMap(@NotNull Map<String, Integer> concurrencyConnectionMap) {
        if (!etcdClient.put(CONCURRENCY_CONNECTION_MAP, JSON.toJSONString(concurrencyConnectionMap))) {
            throw new RuntimeException("更新并发连接配置失败");
        }
    }
}
