package xin.manong.darwin.service.iface;

import com.alibaba.fastjson.JSON;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.ExcelDocumentExporter;
import xin.manong.darwin.service.request.OrderByRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.io.IOException;
import java.io.Serial;
import java.util.*;

/**
 * URL服务接口定义
 *
 * @author frankcl
 * @date 2023-03-20 20:00:56
 */
public abstract class URLService {

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
     * 根据key删除URL记录
     *
     * @param key 唯一key
     * @return 成功返回true，否则返回false
     */
    public abstract boolean delete(String key);

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
     * 根据URL状态分组计数
     *
     * @param jobId 任务ID
     * @param timeRange 时间范围
     * @return 统计结果
     */
    public abstract List<URLGroupCount> countGroupByStatus(String jobId, RangeValue<Long> timeRange);

    /**
     * 根据内容类型分组计数
     *
     * @param jobId 任务ID
     * @param timeRange 时间范围
     * @return 统计结果
     */
    public abstract List<URLGroupCount> countGroupByCategory(String jobId, RangeValue<Long> timeRange);

    /**
     * 统计排队抓取top的并发单元
     *
     * @param top top数量
     * @return 统计结果
     */
    public abstract List<URLGroupCount> topConcurrencyUnits(int top);

    /**
     * 统计时间范围内抓取量top的站点
     *
     * @param timeRange 时间范围
     * @param top top数量
     * @return 统计结果
     */
    public abstract List<URLGroupCount> topHosts(RangeValue<Long> timeRange, int top);

    /**
     * 统计时间范围内抓取URL数量
     *
     * @param timeRange 时间范围
     * @return URL数量
     */
    public abstract int urlCount(RangeValue<Long> timeRange);

    /**
     * 统计时间范围内抓取host数量
     *
     * @param timeRange 时间范围
     * @return host数量
     */
    public abstract int hostCount(RangeValue<Long> timeRange);

    /**
     * 统计时间范围内抓取domain数量
     *
     * @param timeRange 时间范围
     * @return domain数量
     */
    public abstract int domainCount(RangeValue<Long> timeRange);

    /**
     * 平均内容长度
     *
     * @param timeRange 时间范围
     * @param category 内容类型
     * @return 平均内容长度
     */
    public abstract long avgContentLength(Integer category, RangeValue<Long> timeRange);

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
     * 根据URL获取在startTime之后的最新的抓取成功记录
     *
     * @param url URL
     * @param startTime 起始时间
     * @return 成功返回数据记录，否则返回null
     */
    public URLRecord getFetchedByURL(String url, long startTime) {
        OrderByRequest orderByRequest = new OrderByRequest("fetch_time", false);
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = url;
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
     * @param url URL
     * @param planId 计划ID
     * @return 如果抓取过返回true，否则返回false
     */
    public boolean isFetched(String url, String planId) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = url;
        searchRequest.planId = planId;
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
     * @param url URL
     * @param jobId 任务ID
     * @return 存在重复数据返回true，否则返回false
     */
    public boolean isDuplicate(String url, String jobId) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = url;
        searchRequest.jobId = jobId;
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
}
