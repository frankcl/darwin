package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONMediaTypeHandler;
import xin.manong.darwin.common.model.handler.JSONObjectMapHandler;
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
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "url", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLRecord extends SeedRecord {

    private static final Logger logger = LoggerFactory.getLogger(URLRecord.class);

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
     * 并发级别
     * domain:0
     * host:1
     */
    @TableField(value = "concurrency_level")
    @Column(name = "concurrency_level")
    @JSONField(name = "concurrency_level")
    @JsonProperty("concurrency_level")
    public Integer concurrencyLevel;

    /**
     * 并发单元
     */
    @TableField(value = "concurrency_unit")
    @Column(name = "concurrency_unit")
    @JSONField(name = "concurrency_unit")
    @JsonProperty("concurrency_unit")
    public String concurrencyUnit;

    /**
     * 任务ID
     */
    @TableField(value = "job_id")
    @Column(name = "job_id")
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @Column(name = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 内容类型
     * 网页：1
     * 图片：2
     * 视频：3
     * 音频：4
     * 其他：5
     */
    @TableField(value = "content_type")
    @Column(name = "content_type")
    @JSONField(name = "content_type")
    @JsonProperty("content_type")
    public Integer contentType;

    /**
     * 抓取时间
     */
    @TableField(value = "fetch_time")
    @Column(name = "fetch_time")
    @JSONField(name = "fetch_time")
    @JsonProperty("fetch_time")
    public Long fetchTime;

    /**
     * 下载时长
     */
    @TableField(value = "down_time")
    @Column(name = "down_time")
    @JSONField(name = "down_time")
    @JsonProperty("down_time")
    public Long downTime;

    /**
     * 抓取内容OSS地址
     */
    @TableField(value = "fetch_content_url")
    @Column(name = "fetch_content_url")
    @JSONField(name = "fetch_content_url")
    @JsonProperty("fetch_content_url")
    public String fetchContentURL;

    /**
     * URL状态
     */
    @TableField(value = "status")
    @Column(name = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;

    /**
     * http状态码
     */
    @TableField(value = "http_code")
    @Column(name = "http_code")
    @JSONField(name = "http_code")
    @JsonProperty("http_code")
    public Integer httpCode;

    /**
     * 内容字节数
     */
    @TableField(value = "content_length")
    @Column(name = "content_length")
    @JSONField(name = "content_length")
    @JsonProperty("content_length")
    public Long contentLength;

    /**
     * 出队时间
     */
    @TableField(value = "pop_time")
    @Column(name = "pop_time")
    @JSONField(name = "pop_time")
    @JsonProperty("pop_time")
    public Long popTime;

    /**
     * 入队时间
     */
    @TableField(value = "push_time")
    @Column(name = "push_time")
    @JSONField(name = "push_time")
    @JsonProperty("push_time")
    public Long pushTime;

    /**
     * 深度
     */
    @TableField(value = "depth")
    @Column(name = "depth")
    @JSONField(name = "depth")
    @JsonProperty("depth")
    public Integer depth = 0;

    /**
     * 是否抓取
     * true: 通过HTTP抓取
     * false: 存量数据
     */
    @TableField(value = "fetched")
    @Column(name = "fetched")
    @JSONField(name = "fetched")
    @JsonProperty("fetched")
    public Boolean fetched;

    /**
     * 结构化字段
     */
    @TableField(value = "field_map", typeHandler = JSONObjectMapHandler.class)
    @Column(name = "field_map")
    @JSONField(name = "field_map", deserializeUsing = MapDeserializer.class)
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap = new HashMap<>();

    /**
     * 媒体类型
     */
    @TableField(value = "media_type", typeHandler = JSONMediaTypeHandler.class)
    @Column(name = "media_type")
    @JSONField(name = "media_type")
    @JsonProperty("media_type")
    public MediaType mediaType;

    /**
     * 字符集
     */
    @TableField(value = "charset")
    @Column(name = "charset")
    @JSONField(name = "charset")
    @JsonProperty("charset")
    public String charset;

    /**
     * HTML字符集
     */
    @TableField(value = "html_charset")
    @Column(name = "html_charset")
    @JSONField(name = "html_charset")
    @JsonProperty("html_charset")
    public String htmlCharset;

    /**
     * 允许重复抓取
     */
    @TableField(value = "allow_repeat")
    @Column(name = "allow_repeat")
    @JSONField(name = "allow_repeat")
    @JsonProperty("allow_repeat")
    public Boolean allowRepeat;

    /**
     * 文本内容
     */
    @TableField(exist = false)
    public String text;

    /**
     * MimeType
     */
    @JSONField(name = "mime_type")
    @JsonProperty("mime_type")
    @TableField(exist = false)
    public String mimeType;

    public URLRecord() {
        super();
        allowRepeat = false;
        status = Constants.URL_STATUS_UNKNOWN;
    }

    public URLRecord(String url) {
        super(url);
        allowRepeat = false;
        status = Constants.URL_STATUS_UNKNOWN;
    }

    public URLRecord(URLRecord record) {
        super(record);
        appId = record.appId;
        jobId = record.jobId;
        contentType = record.contentType;
        fetchTime = record.fetchTime;
        downTime = record.downTime;
        pushTime = record.pushTime;
        popTime = record.popTime;
        parentURL = record.parentURL;
        redirectURL = record.redirectURL;
        fetchContentURL = record.fetchContentURL;
        mediaType = record.mediaType;
        charset = record.charset;
        htmlCharset = record.htmlCharset;
        status = record.status;
        depth = record.depth;
        httpCode = record.httpCode;
        contentLength = record.contentLength;
        concurrencyLevel = record.concurrencyLevel;
        concurrencyUnit = record.concurrencyUnit;
        allowRepeat = record.allowRepeat;
        fetched = record.fetched;
        text = record.text;
        mimeType = record.mimeType;
        fieldMap = record.fieldMap == null ? new HashMap<>() : new HashMap<>(record.fieldMap);
    }

    /**
     * 判断是否为溢出数据
     *
     * @param maxTimeIntervalMs 最大溢出时间间隔
     * @return 溢出返回true，否则返回false
     */
    public boolean isOverflow(long maxTimeIntervalMs) {
        return pushTime == null || System.currentTimeMillis() - pushTime > maxTimeIntervalMs;
    }

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (!super.check()) return false;
        if (StringUtils.isEmpty(jobId)) {
            logger.error("Job id is empty");
            return false;
        }
        if (appId == null) {
            logger.error("App id is null");
            return false;
        }
        if (depth == null || depth < 0) depth = 0;
        if (allowRepeat == null) allowRepeat = false;
        if (status == null) status = Constants.URL_STATUS_UNKNOWN;
        return true;
    }

    /**
     * 构建范围抽链链接
     *
     * @param url URL
     * @return 数据
     */
    public static URLRecord scopeLink(String url, int linkScope) {
        assert Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope);
        URLRecord record = new URLRecord(url);
        record.linkScope = linkScope;
        record.allowRepeat = false;
        return record;
    }
}
