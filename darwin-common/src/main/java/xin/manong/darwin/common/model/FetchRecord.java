package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONMapObjectTypeHandler;
import xin.manong.weapon.aliyun.ots.annotation.Column;
import xin.manong.weapon.aliyun.ots.annotation.PrimaryKey;
import xin.manong.weapon.base.util.RandomID;

import java.util.HashMap;
import java.util.Map;

/**
 * 抓取信息
 *
 * @author frankcl
 * @date 2023-03-06 14:28:13
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchRecord extends BasicModel {

    private static final Logger logger = LoggerFactory.getLogger(FetchRecord.class);

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
     * 抓取时间
     */
    @TableField(value = "fetch_time")
    @Column(name = "fetch_time")
    @JSONField(name = "fetch_time")
    @JsonProperty("fetch_time")
    public Long fetchTime;

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
    @JSONField(name = "field_map")
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap = new HashMap<>();

    public FetchRecord() {
        key = RandomID.build();
        status = Constants.URL_STATUS_CREATED;
    }

    public FetchRecord(String url) {
        this();
        this.url = url;
        this.hash = DigestUtils.md5Hex(url);
    }

    public FetchRecord(FetchRecord record) {
        this.key = record.key;
        this.hash = record.hash;
        this.url = record.url;
        this.jobId = record.jobId;
        this.planId = record.planId;
        this.fetchTime = record.fetchTime;
        this.parentURL = record.parentURL;
        this.redirectURL = record.redirectURL;
        this.fetchContentURL = record.fetchContentURL;
        this.mimeType = record.mimeType;
        this.subMimeType = record.subMimeType;
        this.status = record.status;
        this.userDefinedMap = record.userDefinedMap;
        this.fieldMap = record.fieldMap;
    }

    /**
     * 重新构建key
     */
    public void rebuildKey() {
        key = RandomID.build();
    }

    /**
     * 检测有效性
     * 1. url不能为空
     * 2. key不能为空
     * 3. hash不能为空
     * 4. jobId不能为空
     * 5. status合法
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(key)) {
            logger.error("key is empty");
            return false;
        }
        if (StringUtils.isEmpty(hash)) {
            logger.error("hash is empty");
            return false;
        }
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
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
        if (status == null) status = Constants.URL_STATUS_CREATED;
        else if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) {
            logger.error("not support url status[{}]", status);
            return false;
        }
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
