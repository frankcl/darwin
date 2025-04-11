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
import xin.manong.darwin.common.model.handler.JSONMapObjectTypeHandler;
import xin.manong.darwin.common.model.json.MapDeserializer;
import xin.manong.weapon.aliyun.ots.annotation.Column;

import java.io.Serial;
import java.nio.charset.Charset;
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
    @Serial
    private static final long serialVersionUID = 4425209680844398546L;

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
     * 结构化字段
     */
    @TableField(value = "field_map", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "field_map")
    @JSONField(name = "field_map", deserializeUsing = MapDeserializer.class)
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap = new HashMap<>();

    /**
     * HTTP头编码
     */
    @TableField(exist = false)
    public Charset charset;

    /**
     * HTML内容
     */
    @TableField(exist = false)
    public String html;

    public URLRecord() {
        super();
        status = Constants.URL_STATUS_CREATED;
    }

    public URLRecord(String url) {
        super(url);
        status = Constants.URL_STATUS_CREATED;
    }

    public URLRecord(URLRecord record) {
        super(record);
        appId = record.appId;
        jobId = record.jobId;
        fetchTime = record.fetchTime;
        pushTime = record.pushTime;
        popTime = record.popTime;
        parentURL = record.parentURL;
        redirectURL = record.redirectURL;
        fetchContentURL = record.fetchContentURL;
        mimeType = record.mimeType;
        subMimeType = record.subMimeType;
        status = record.status;
        depth = record.depth;
        httpCode = record.httpCode;
        fieldMap = record.fieldMap == null ? new HashMap<>() : new HashMap<>(record.fieldMap);
        charset = record.charset;
        html = record.html;
    }

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (!super.check()) return false;
        if (StringUtils.isEmpty(jobId)) {
            logger.error("job id is empty");
            return false;
        }
        if (appId == null) {
            logger.error("app id is null");
            return false;
        }
        if (depth == null || depth < 0) depth = 0;
        if (status == null) status = Constants.URL_STATUS_CREATED;
        if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) {
            logger.error("not support url status[{}]", status);
            return false;
        }
        return true;
    }
}
