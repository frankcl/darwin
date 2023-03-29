package xin.manong.darwin.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueStatus;
import xin.manong.darwin.service.iface.MultiQueueService;
import xin.manong.darwin.service.iface.URLService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 多级队列服务实现
 *
 * @author frankcl
 * @date 2023-03-28 14:42:28
 */
@Service
public class MultiQueueServiceImpl implements MultiQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueueServiceImpl.class);

    private int retryCnt = 3;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected URLService urlService;

    @Override
    public URLRecord pushQueue(URLRecord record) {
        for (int i = 0; i < retryCnt; i++) {
            MultiQueueStatus status = multiQueue.push(record);
            if (status == MultiQueueStatus.ERROR) {
                logger.error("push record[{}] error", record.url);
                record.status = Constants.URL_STATUS_INVALID;
                break;
            } else if (status == MultiQueueStatus.REFUSED || status == MultiQueueStatus.FULL) {
                logger.warn("push record[{}] refused, reason[{}]", record.url, status.name());
                record.status = Constants.URL_STATUS_QUEUING_REFUSED;
            } else {
                record.status = Constants.URL_STATUS_QUEUING;
                break;
            }
        }
        URLRecord prevRecord = urlService.get(record.key);
        boolean status = prevRecord == null ? urlService.add(record) : urlService.updateQueueTime(record);
        if (!status) logger.warn("{} seed record[{}] failed", prevRecord == null ? "add" : "update", record.key);
        return record;
    }

    @Override
    public List<URLRecord> pushQueue(List<URLRecord> records) {
        List<URLRecord> processRecords = new ArrayList<>();
        if (records == null || records.isEmpty()) return processRecords;
        for (URLRecord record : records) processRecords.add(pushQueue(record));
        return processRecords;
    }
}
