package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONMapObjectTypeHandler;
import xin.manong.darwin.common.model.json.MapDeserializer;
import xin.manong.weapon.aliyun.ots.annotation.Column;
import xin.manong.weapon.aliyun.ots.annotation.PrimaryKey;
import xin.manong.weapon.base.util.RandomID;

import java.io.Serial;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * URL信息
 *
 * @author frankcl
 * @date 2023-03-06 14:28:13
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "url", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLRecord extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(URLRecord.class);
    @Serial
    private static final long serialVersionUID = 4425209680844398546L;

    /**
     * 唯一key
     */
    @TableId(value = "`key`")
    @PrimaryKey(name = "key")
    @JSONField(name = "key")
    @JsonProperty("key")
    public String key;

    /**
     * URL hash
     */
    @TableField(value = "hash")
    @Column(name = "hash")
    @JSONField(name = "hash")
    @JsonProperty("hash")
    public String hash;

    /**
     * 抓取URL
     */
    @TableField(value = "url")
    @Column(name = "url")
    @JSONField(name = "url")
    @JsonProperty("url")
    public String url;

    /**
     * 重定向URL
     */
    @TableField(value = "redirect_url")
    @Column(name = "redirect_url")
    @JSONField(name = "redirect_url")
    @JsonProperty("redirect_url")
    public String redirectURL;

    /**
     * 父URL
     */
    @TableField(value = "parent_url")
    @Column(name = "parent_url")
    @JSONField(name = "parent_url")
    @JsonProperty("parent_url")
    public String parentURL;

    /**
     * 任务ID
     */
    @TableField(value = "job_id")
    @Column(name = "job_id")
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 计划ID
     */
    @TableField(value = "plan_id")
    @Column(name = "plan_id")
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @Column(name = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 抓取时间
     */
    @TableField(value = "fetch_time")
    @Column(name = "fetch_time")
    @JSONField(name = "fetch_time")
    @JsonProperty("fetch_time")
    public Long fetchTime;

    /**
     * 抓取内容OSS地址
     */
    @TableField(value = "fetch_content_url")
    @Column(name = "fetch_content_url")
    @JSONField(name = "fetch_content_url")
    @JsonProperty("fetch_content_url")
    public String fetchContentURL;

    /**
     * mimeType
     */
    @TableField(value = "mime_type")
    @Column(name = "mime_type")
    @JSONField(name = "mime_type")
    @JsonProperty("mime_type")
    public String mimeType;

    /**
     * 子mimeType
     */
    @TableField(value = "sub_mime_type")
    @Column(name = "sub_mime_type")
    @JSONField(name = "sub_mime_type")
    @JsonProperty("sub_mime_type")
    public String subMimeType;

    /**
     * URL状态
     */
    @TableField(value = "status")
    @Column(name = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;

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
     * http状态码
     */
    @TableField(value = "http_code")
    @Column(name = "http_code")
    @JSONField(name = "http_code")
    @JsonProperty("http_code")
    public Integer httpCode;

    /**
     * 出队时间
     */
    @TableField(value = "out_queue_time")
    @Column(name = "out_queue_time")
    @JSONField(name = "out_queue_time")
    @JsonProperty("out_queue_time")
    public Long outQueueTime;

    /**
     * 入队时间
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
    public Integer scope;

    /**
     * 抓取并发级别
     */
    @TableField(value = "concurrent_level")
    @Column(name = "concurrent_level")
    @JSONField(name = "concurrent_level")
    @JsonProperty("concurrent_level")
    public Integer concurrentLevel;

    /**
     * 用户定义字段，透传到抓取结果
     */
    @TableField(value = "user_defined_map", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "user_defined_map")
    @JSONField(name = "user_defined_map")
    @JsonProperty("user_defined_map")
    public Map<String, Object> userDefinedMap = new HashMap<>();

    /**
     * 结构化字段
     */
    @TableField(value = "field_map", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "field_map")
    @JSONField(name = "field_map", deserializeUsing = MapDeserializer.class)
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap = new HashMap<>();

    /**
     * HTTP header信息
     */
    @TableField(value = "headers", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "headers")
    @JSONField(name = "headers", deserializeUsing = MapDeserializer.class)
    @JsonProperty("headers")
    public Map<String, String> headers = new HashMap<>();

    /**
     * HTTP头编码
     */
    @TableField(exist = false)
    public Charset charset;

    public URLRecord() {
        key = RandomID.build();
        status = Constants.URL_STATUS_CREATED;
        createTime = System.currentTimeMillis();
    }

    public URLRecord(String url) {
        this();
        this.url = url;
        this.hash = DigestUtils.md5Hex(url);
    }

    public URLRecord(URLRecord record) {
        key = record.key;
        hash = record.hash;
        url = record.url;
        appId = record.appId;
        jobId = record.jobId;
        planId = record.planId;
        fetchTime = record.fetchTime;
        createTime = record.createTime;
        updateTime = record.updateTime;
        inQueueTime = record.inQueueTime;
        outQueueTime = record.outQueueTime;
        parentURL = record.parentURL;
        redirectURL = record.redirectURL;
        fetchContentURL = record.fetchContentURL;
        mimeType = record.mimeType;
        subMimeType = record.subMimeType;
        status = record.status;
        concurrentLevel = record.concurrentLevel;
        timeout = record.timeout;
        fetchMethod = record.fetchMethod;
        priority = record.priority;
        category = record.category;
        depth = record.depth;
        scope = record.scope;
        httpCode = record.httpCode;
        userDefinedMap = record.userDefinedMap == null ? new HashMap<>() : new HashMap<>(record.userDefinedMap);
        fieldMap = record.fieldMap == null ? new HashMap<>() : new HashMap<>(record.fieldMap);
        headers = record.headers == null ? new HashMap<>() : new HashMap<>(record.headers);
    }

    /**
     * 是否使用代理抓取
     *
     * @return 使用代理返回true，否则返回false
     */
    public boolean useProxy() {
        return fetchMethod != null && (fetchMethod == Constants.FETCH_METHOD_LONG_PROXY ||
                fetchMethod == Constants.FETCH_METHOD_SHORT_PROXY);
    }

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(key)) {
            logger.error("key is empty");
            return false;
        }
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            return false;
        }
        if (StringUtils.isEmpty(hash)) {
            logger.error("hash is empty");
            return false;
        }
        if (StringUtils.isEmpty(jobId)) {
            logger.error("job id is empty");
            return false;
        }
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            return false;
        }
        if (appId == null) {
            logger.error("app id is null");
            return false;
        }
        if (category == null) category = Constants.CONTENT_CATEGORY_CONTENT;
        if (!Constants.SUPPORT_CONTENT_CATEGORIES.containsKey(category)) {
            logger.error("not support content category[{}]", category);
            return false;
        }
        if (status == null) status = Constants.URL_STATUS_CREATED;
        if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) {
            logger.error("not support url status[{}]", status);
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
     * 是否全局抽链，满足以下条件为全局抽链
     * 1. 列表页
     * 2. 抽链范围scope合法
     *
     * @return 全局抽链返回true，否则返回false
     */
    public boolean isScopeExtract() {
        return category != null && category == Constants.CONTENT_CATEGORY_LIST &&
                scope != null && Constants.SUPPORT_LINK_SCOPES.containsKey(scope);
    }

    /**
     * 重新构建key
     */
    public void rebuildKey() {
        key = RandomID.build();
    }

    /**
     * 设置URL，设置hash
     *
     * @param url URL
     */
    public void setUrl(String url) {
        this.url = url;
        this.hash = DigestUtils.md5Hex(url);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof URLRecord other)) return false;
        if (object == this) return true;
        return Objects.equals(key, other.key);
    }

    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }
}
