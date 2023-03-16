package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.base.util.RandomID;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * URL信息
 *
 * @author frankcl
 * @date 2023-03-06 14:28:13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLRecord implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(URLRecord.class);

    /**
     * 超时时间（毫秒）
     */
    @JSONField(name = "timeout")
    @JsonProperty("timeout")
    public Integer timeout;

    /**
     * 唯一key
     */
    @JSONField(name = "key")
    @JsonProperty("key")
    public String key;

    /**
     * 任务ID
     */
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 优先级
     */
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 创建时间
     */
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;

    /**
     * 抓取时间
     */
    @JSONField(name = "fetch_time")
    @JsonProperty("fetch_time")
    public Long fetchTime;

    /**
     * 出对时间
     */
    @JSONField(name = "out_queue_time")
    @JsonProperty("out_queue_time")
    public Long outQueueTime;

    /**
     * 进入多级队列时间
     */
    @JSONField(name = "in_queue_time")
    @JsonProperty("in_queue_time")
    public Long inQueueTime;

    /**
     * 抓取URL
     */
    @JSONField(name = "url")
    @JsonProperty("url")
    public String url;

    /**
     * 父URL
     */
    @JSONField(name = "parent_url")
    @JsonProperty("parent_url")
    public String parentURL;

    /**
     * 抓取URL类型
     */
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * URL状态
     */
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;

    /**
     * 抓取并发级别
     */
    @JSONField(name = "concurrent_level")
    @JsonProperty("concurrent_level")
    public Integer concurrentLevel;

    /**
     * HTTP header信息
     */
    @JSONField(name = "headers")
    @JsonProperty("headers")
    public Map<String, Object> headers = new HashMap<>();

    /**
     * 用户定义字段，透传到抓取结果
     */
    @JSONField(name = "user_defined_map")
    @JsonProperty("user_defined_map")
    public Map<String, Object> userDefinedMap = new HashMap<>();

    public URLRecord() {
        key = RandomID.build();
        status = Constants.URL_STATUS_CREATED;
        createTime = System.currentTimeMillis();
    }

    public URLRecord(String url) {
        this();
        this.url = url;
    }

    /**
     * 检测URLRecord有效性
     * 1. url不能为空
     * 2. category不能为空
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            return false;
        }
        if (StringUtils.isEmpty(jobId)) {
            logger.error("job id is empty");
            return false;
        }
        if (!Constants.SUPPORT_CONTENT_CATEGORIES.contains(category)) {
            logger.error("not support content category[{}]", category);
            return false;
        }
        if (status == null) status = Constants.URL_STATUS_CREATED;
        else if (!Constants.SUPPORT_URL_STATUSES.contains(status)) {
            logger.error("not support url status[{}]", status);
            return false;
        }
        if (concurrentLevel == null) concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
        if (!Constants.SUPPORT_CONCURRENT_LEVELS.contains(concurrentLevel)) {
            logger.error("not support concurrent level[{}]", concurrentLevel);
            return false;
        }
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof URLRecord)) return false;
        URLRecord other = (URLRecord) object;
        if (other == this || key == other.key) return true;
        if (key == null || other.key == null) return false;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }
}
