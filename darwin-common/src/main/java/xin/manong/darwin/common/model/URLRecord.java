package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONMapObjectTypeHandler;
import xin.manong.weapon.aliyun.ots.annotation.Column;

import java.util.HashMap;
import java.util.Map;

/**
 * URL信息
 *
 * @author frankcl
 * @date 2023-03-06 14:28:13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "url", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLRecord extends FetchRecord {

    private static final Logger logger = LoggerFactory.getLogger(URLRecord.class);

    /**
     * 超时时间（毫秒）
     */
    @TableField(value = "timeout")
    @Column(name = "timeout")
    @JSONField(name = "timeout")
    @JsonProperty("timeout")
    public Integer timeout;

    /**
     * 优先级
     */
    @TableField(value = "priority")
    @Column(name = "priority")
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 抓取方式
     */
    @TableField(value = "fetch_method")
    @Column(name = "fetch_method")
    @JSONField(name = "fetch_method")
    @JsonProperty("fetch_method")
    public Integer fetchMethod;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Column(name = "create_time")
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Column(name = "update_time")
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

    /**
     * 出对时间
     */
    @TableField(value = "out_queue_time")
    @Column(name = "out_queue_time")
    @JSONField(name = "out_queue_time")
    @JsonProperty("out_queue_time")
    public Long outQueueTime;

    /**
     * 进入多级队列时间
     */
    @TableField(value = "in_queue_time")
    @Column(name = "in_queue_time")
    @JSONField(name = "in_queue_time")
    @JsonProperty("in_queue_time")
    public Long inQueueTime;

    /**
     * 抓取URL类型
     */
    @TableField(value = "category")
    @Column(name = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * 深度
     */
    @TableField(value = "depth")
    @Column(name = "depth")
    @JSONField(name = "depth")
    @JsonProperty("depth")
    public Integer depth = 0;

    /**
     * 抓取并发级别
     */
    @TableField(value = "concurrent_level")
    @Column(name = "concurrent_level")
    @JSONField(name = "concurrent_level")
    @JsonProperty("concurrent_level")
    public Integer concurrentLevel;

    /**
     * HTTP header信息
     */
    @TableField(value = "headers", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "headers")
    @JSONField(name = "headers")
    @JsonProperty("headers")
    public Map<String, Object> headers = new HashMap<>();

    public URLRecord() {
        super();
        createTime = System.currentTimeMillis();
    }

    public URLRecord(String url) {
        super(url);
        this.url = url;
        this.createTime = System.currentTimeMillis();
    }

    public URLRecord(URLRecord record) {
        super(record);
        this.concurrentLevel = record.concurrentLevel;
        this.timeout = record.timeout;
        this.priority = record.priority;
        this.createTime = record.createTime;
        this.updateTime = record.updateTime;
        this.inQueueTime = record.inQueueTime;
        this.outQueueTime = record.outQueueTime;
        this.category = record.category;
        this.depth = record.depth;
        this.headers = record.headers;
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
        if (!Constants.SUPPORT_CONTENT_CATEGORIES.containsKey(category)) {
            logger.error("not support content category[{}]", category);
            return false;
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("not support fetch method[{}]", fetchMethod);
            return false;
        }
        if (status == null) status = Constants.URL_STATUS_CREATED;
        else if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) {
            logger.error("not support url status[{}]", status);
            return false;
        }
        if (concurrentLevel == null) concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
        if (depth == null || depth < 0) depth = 0;
        if (!Constants.SUPPORT_CONCURRENT_LEVELS.containsKey(concurrentLevel)) {
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
