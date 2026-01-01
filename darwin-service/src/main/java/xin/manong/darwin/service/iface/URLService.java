package xin.manong.darwin.service.iface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.ExcelDocumentExporter;
import xin.manong.darwin.service.component.Message;
import xin.manong.darwin.service.component.MessagePusher;
import xin.manong.darwin.service.component.PushResult;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.lineage.Node;
import xin.manong.darwin.service.request.OrderByRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * URL服务接口定义
 *
 * @author frankcl
 * @date 2023-03-20 20:00:56
 */
public abstract class URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLService.class);

    @Resource
    private ServiceConfig config;
    @Resource
    private MessagePusher pusher;

    protected static List<String> EXPORT_COLUMNS = new ArrayList<>() {
        @Serial
        private static final long serialVersionUID = 3442274479484811098L;

        {
        add("key");
        add("url");
        add("redirect_url");
        add("parent_url");
        add("fetch_content_url");
        add("job_id");
        add("plan_id");
        add("fetch_time");
        add("status");
        add("http_code");
        add("custom_map");
        add("field_map");
    }};

    /**
     * 添加URL记录
     *
     * @param record url记录
     * @return 添加成功返回true，否则返回false
     */
    public abstract boolean add(URLRecord record);

    /**
     * 更新抓取结果
     *
     * @param record 抓取结果
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean updateContent(URLRecord record);

    /**
     * 更新入队出队时间
     *
     * @param record URL记录
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean updateQueueTime(URLRecord record);

    /**
     * 更新URL状态
     *
     * @param key URL key
     * @param status 状态
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean updateStatus(String key, int status);

    /**
     * 根据key获取URL记录
     *
     * @param key 唯一key
     * @return URL记录，无记录返回null
     */
    public abstract URLRecord get(String key);

    /**
     * 根据父key获取孩子列表
     *
     * @param parentKey 父key
     * @return 孩子列表
     */
    public abstract List<URLRecord> getChildren(String parentKey);

    /**
     * 根据key删除URL记录
     *
     * @param key 唯一key
     * @return 成功返回true，否则返回false
     */
    public abstract boolean delete(String key);

    /**
     * 删除任务相关数据
     *
     * @param jobId 任务ID
     * @return 删除成功返回true，否则返回false
     */
    public abstract boolean deleteByJob(String jobId);

    /**
     * 删除过期数据：创建时间小于expiredTime
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    public abstract int deleteExpired(long expiredTime);

    /**
     * 搜索URL列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    public abstract Pager<URLRecord> search(URLSearchRequest searchRequest);

    /**
     * 获取数量
     *
     * @param searchRequest 搜索请求
     * @return 数量
     */
    public abstract long selectCount(URLSearchRequest searchRequest);

    /**
     * 状态分组计数
     *
     * @param jobId 任务ID
     * @param timeRange 时间范围
     * @return 统计列表
     */
    public abstract List<URLGroupCount> statusGroupCount(String jobId, RangeValue<Long> timeRange);

    /**
     * 内容分组统计
     *
     * @param jobId 任务ID
     * @param timeRange 时间范围
     * @return 统计列表
     */
    public abstract List<URLGroupCount> contentGroupCount(String jobId, RangeValue<Long> timeRange);

    /**
     * TOP排队等待并发单元列表
     *
     * @param n top数量
     * @return 并发单元列表
     */
    public abstract List<URLGroupCount> waitConcurrencyUnits(int n);

    /**
     * 排队等待数据优先级分布统计
     *
     * @return 优先级分布统计列表
     */
    public abstract List<URLGroupCount> queueWaitPriority();

    /**
     * TOP抓取量站点列表
     *
     * @param timeRange 时间范围
     * @param n top数量
     * @return 站点列表
     */
    public abstract List<URLGroupCount> hostFetchCount(RangeValue<Long> timeRange, int n);

    /**
     * 统计时间范围内抓取URL数量
     *
     * @param timeRange 时间范围
     * @return URL数量
     */
    public abstract int fetchURLCount(RangeValue<Long> timeRange);

    /**
     * 统计时间范围内抓取host数量
     *
     * @param timeRange 时间范围
     * @return host数量
     */
    public abstract int fetchHostCount(RangeValue<Long> timeRange);

    /**
     * 统计时间范围内抓取domain数量
     *
     * @param timeRange 时间范围
     * @return domain数量
     */
    public abstract int fetchDomainCount(RangeValue<Long> timeRange);

    /**
     * 准备搜索请求
     *
     * @param searchRequest 搜索请求
     * @return 搜索请求
     */
    protected URLSearchRequest prepareSearchRequest(URLSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new URLSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        List<Integer> statusList = ModelValidator.validateListField(searchRequest.status, Integer.class);
        if (statusList != null && !statusList.isEmpty()) searchRequest.statusList = statusList;
        RangeValue<Long> rangeValue = ModelValidator.validateRangeValue(searchRequest.fetchTime, Long.class);
        if (rangeValue != null) searchRequest.fetchTimeRange = rangeValue;
        rangeValue = ModelValidator.validateRangeValue(searchRequest.createTime, Long.class);
        if (rangeValue != null) searchRequest.createTimeRange = rangeValue;
        return searchRequest;
    }

    /**
     * 计算指定数据类型时间范围内平均下载时间
     *
     * @param contentType 数据类型
     * @param timeRange 时间范围
     * @return 平均下载时间
     */
    public long avgDownTime(Integer contentType, RangeValue<Long> timeRange) {
        long downTime = 0L, recordCount = 0L;
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.pageSize = 5000;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_FAIL);
        searchRequest.statusList.add(Constants.URL_STATUS_ERROR);
        searchRequest.fetchTimeRange = timeRange;
        searchRequest.contentType = contentType;
        while (true) {
            Pager<URLRecord> pager = search(searchRequest);
            for (URLRecord record : pager.records) {
                if (record.downTime == null) continue;
                downTime += record.downTime;
                recordCount++;
            }
            if (pager.records.size() < searchRequest.pageSize) break;
            searchRequest.pageNum++;
        }
        return recordCount == 0L ? 0L : downTime / recordCount;
    }

    /**
     * 计算指定数据类型时间范围内平均内容长度
     *
     * @param contentType 数据类型
     * @param timeRange 时间范围
     * @return 平均内容长度
     */
    public long avgContentLength(Integer contentType, RangeValue<Long> timeRange) {
        long contentLength = 0L, recordCount = 0L;
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.pageSize = 5000;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        searchRequest.fetchTimeRange = timeRange;
        searchRequest.contentType = contentType;
        while (true) {
            Pager<URLRecord> pager = search(searchRequest);
            for (URLRecord record : pager.records) {
                if (record.contentLength == null) continue;
                contentLength += record.contentLength;
                recordCount++;
            }
            if (pager.records.size() < searchRequest.pageSize) break;
            searchRequest.pageNum++;
        }
        return recordCount == 0L ? 0L : contentLength / recordCount;
    }

    /**
     * 计算并发单元排队等待数据量
     * 如果并发单元为null，则计算所有排队等待数据量
     *
     * @param concurrencyUnit 并发单元
     * @return 排队等待数据量
     */
    public long queueWaitCount(String concurrencyUnit) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.concurrencyUnit = concurrencyUnit;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        return selectCount(searchRequest);
    }

    /**
     * 计算并发单元正在抓取数据量
     * 如果并发单元为null，则计算所有正在抓取数据量
     *
     * @param concurrencyUnit 并发单元
     * @return 正在抓取数据量
     */
    public long fetchingCount(String concurrencyUnit) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.concurrencyUnit = concurrencyUnit;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        return selectCount(searchRequest);
    }

    /**
     * 计算并发单元平均排队等待时间
     * 如果并发单元为null，则计算所有排队数据平均等待时间
     *
     * @param concurrencyUnit 并发单元
     * @return 平均排队等待时间
     */
    public long queueWaitTime(String concurrencyUnit) {
        long waitTime = 0L;
        long recordCount = 0L;
        long currentTime = System.currentTimeMillis();
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.pageNum = 1;
        searchRequest.pageSize = 10000;
        searchRequest.concurrencyUnit = concurrencyUnit;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        while (true) {
            Pager<URLRecord> pager = search(searchRequest);
            for (URLRecord record : pager.records) {
                if (record.pushTime == null) continue;
                waitTime += currentTime - record.pushTime;
                recordCount++;
            }
            if (pager.records.size() < searchRequest.pageSize) break;
            searchRequest.pageNum++;
        }
        return recordCount == 0L ? 0L : waitTime / recordCount;
    }

    /**
     * 获取在startTime之后的最新的抓取成功记录
     *
     * @param record 数据
     * @param startTime 起始时间
     * @return 成功返回数据记录，否则返回null
     */
    public URLRecord getFetched(URLRecord record, long startTime) {
        OrderByRequest orderByRequest = new OrderByRequest("fetch_time", false);
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = record.url;
        searchRequest.requestBody = record.requestBody;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        searchRequest.fetchTimeRange = new RangeValue<>();
        searchRequest.fetchTimeRange.includeLower = true;
        searchRequest.fetchTimeRange.start = startTime;
        searchRequest.orderByRequests = new ArrayList<>();
        searchRequest.orderByRequests.add(orderByRequest);
        searchRequest.pageNum = searchRequest.pageSize = 1;
        Pager<URLRecord> pager = search(searchRequest);
        if (pager == null || pager.records.isEmpty()) return null;
        return pager.records.get(0);
    }

    /**
     * 判断计划是否抓取过URL
     *
     * @param record 数据
     * @return 如果抓取过返回true，否则返回false
     */
    public boolean isFetched(URLRecord record) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = record.url;
        searchRequest.planId = record.planId;
        searchRequest.requestBody = record.requestBody;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        searchRequest.pageNum = searchRequest.pageSize = 1;
        return selectCount(searchRequest) > 0;
    }

    /**
     * 是否为任务中重复数据
     *
     * @param record 数据
     * @return 存在重复数据返回true，否则返回false
     */
    public boolean isDuplicate(URLRecord record) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = record.url;
        searchRequest.jobId = record.jobId;
        searchRequest.requestBody = record.requestBody;
        searchRequest.pageNum = searchRequest.pageSize = 1;
        return selectCount(searchRequest) > 0;
    }

    /**
     * 获取指定任务的创建时间小于等于endTime的URL记录
     *
     * @param jobId 任务ID
     * @param endTime 最小创建时间
     * @param size 数量
     * @return URL列表
     */
    public List<URLRecord> getExpiredRecords(String jobId, long endTime, int size) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.pageNum = 1;
        searchRequest.pageSize = size <= 0 ? Constants.DEFAULT_PAGE_SIZE : size;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        searchRequest.createTimeRange = new RangeValue<>();
        searchRequest.createTimeRange.end = endTime;
        searchRequest.createTimeRange.includeUpper = true;
        searchRequest.jobId = jobId;
        Pager<URLRecord> pager = search(searchRequest);
        if (pager == null || pager.records.isEmpty()) return new ArrayList<>();
        return pager.records;
    }

    /**
     * 根据key获取血缘节点
     *
     * @param key 数据key
     * @return 血缘节点
     */
    public Node getLineageNode(String key) {
        URLRecord record = get(key);
        if (record == null) throw new NotFoundException("数据不存在");
        return Converter.convert(record);
    }

    /**
     * 根据父key获取血统孩子列表
     *
     * @param parentKey 父key
     * @return 血统孩子列表
     */
    public List<Node> getLineageChildren(String parentKey) {
        List<URLRecord> children = getChildren(parentKey);
        return children.stream().map(Converter::convert).toList();
    }

    /**
     * 导出URL
     * 最多导出10000条记录
     *
     * @param searchRequest 搜索请求
     * @return 成功返回ExcelDocumentExporter实例，否则返回null
     * @throws IOException I/O异常
     */
    public ExcelDocumentExporter export(URLSearchRequest searchRequest) throws IOException {
        int pageSize = 100;
        if (searchRequest == null) searchRequest = new URLSearchRequest();
        searchRequest.pageNum = 1;
        searchRequest.pageSize = pageSize;
        ExcelDocumentExporter exporter = new ExcelDocumentExporter();
        String sheetName = "下载数据";
        exporter.buildSheet(sheetName, EXPORT_COLUMNS);
        int exportCount = 0;
        while (true) {
            Pager<URLRecord> pager = search(searchRequest);
            for (URLRecord record : pager.records) {
                Map<String, Object> data = JSON.parseObject(JSON.toJSONString(record));
                exporter.add(sheetName, data);
                if (++exportCount >= 10000) break;
            }
            if (pager.records.size() < pageSize) break;
            searchRequest.pageNum++;
        }
        return exporter;
    }

    /**
     * 分发数据
     *
     * @param record URL数据
     * @return 分发结果
     */
    public PushResult dispatch(URLRecord record) {
        if (record == null) return null;
        String recordString = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect);
        Message message = new Message(config.mq.topicURL, record.appId == null ? null : String.valueOf(record.appId),
                record.key, recordString.getBytes(StandardCharsets.UTF_8));
        PushResult pushResult = pusher.pushMessage(message);
        if (pushResult == null) logger.error("Push record message failed for key:{}", record.key);
        return pushResult;
    }
}
