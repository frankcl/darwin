package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONMapObjectTypeHandler;
import xin.manong.darwin.common.model.json.MapDeserializer;
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
     * 应用ID
     */
    @TableField(value = "app_id")
    @Column(name = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

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
     * 全局抽链范围：其他表示不做全局抽链
     * 所有all：1
     * 域domain：2
     * 站点host：3
     */
    @TableField(value = "scope")
    @Column(name = "scope")
    @JSONField(name = "scope")
    @JsonProperty("scope")
    public Integer scope = 0;

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
    @JSONField(name = "headers", deserializeUsing = MapDeserializer.class)
    @JsonProperty("headers")
    public Map<String, String> headers = new HashMap<>();

    public URLRecord() {
        super();
        createTime = System.currentTimeMillis();
    }

    public URLRecord(String url) {
        super(url);
        createTime = System.currentTimeMillis();
    }

    public URLRecord(URLRecord record) {
        super(record);
        concurrentLevel = record.concurrentLevel;
        timeout = record.timeout;
        appId = record.appId;
        fetchMethod = record.fetchMethod;
        priority = record.priority;
        createTime = record.createTime;
        updateTime = record.updateTime;
        inQueueTime = record.inQueueTime;
        outQueueTime = record.outQueueTime;
        category = record.category;
        depth = record.depth;
        scope = record.scope;
        headers = record.headers == null ? new HashMap<>() : new HashMap<>(record.headers);
    }

    /**
     * 检测有效性，满足FetchRecord合法性同时满足以下条件
     * 1. category合法
     * 2. fetchMethod合法
     * 2. concurrentLevel合法
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (!super.check()) return false;
        if (appId == null) {
            logger.error("app id is null");
            return false;
        }
        if (category == null) category = Constants.CONTENT_CATEGORY_CONTENT;
        if (!Constants.SUPPORT_CONTENT_CATEGORIES.containsKey(category)) {
            logger.error("not support content category[{}]", category);
            return false;
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("not support fetch method[{}]", fetchMethod);
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

    /**
     * 是否全局抽链
     *
     * @return 全局抽链返回true，否则返回false
     */
    public boolean isExtractLinkGlobally() {
        return category == Constants.CONTENT_CATEGORY_LIST &&
                scope != null && Constants.SUPPORT_LINK_SCOPES.containsKey(scope);
    }
}
