package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.ConcurrencyControlConfig;

import java.util.Map;

/**
 * 并发单元及并发级别计算器
 *
 * @author frankcl
 * @date 2025-04-28 11:52:39
 */
@Component
public class ConcurrencyComputer {

    @Resource
    private ConcurrencyControlConfig concurrencyControlConfig;

    /**
     * 计算并发级别和并发单元
     *
     * @param record 数据
     */
    public void compute(URLRecord record) {
        Map<String, Integer> concurrencyConnectionMap = concurrencyControlConfig.concurrencyConnectionMap;
        if (concurrencyConnectionMap.containsKey(record.host)) {
            record.concurrencyLevel = Constants.CONCURRENCY_LEVEL_HOST;
            record.concurrencyUnit = record.host;
        } else if (concurrencyConnectionMap.containsKey(record.domain)) {
            record.concurrencyLevel = Constants.CONCURRENCY_LEVEL_DOMAIN;
            record.concurrencyUnit = record.domain;
        } else {
            record.concurrencyLevel = Constants.CONCURRENCY_LEVEL_HOST;
            record.concurrencyUnit = record.host;
        }
    }
}
