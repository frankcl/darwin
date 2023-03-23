package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
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
public class FetchRecord extends Model {

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
    @TableField(value = "structure_map", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "structure_map")
    @JSONField(name = "structure_map")
    @JsonProperty("structure_map")
    public Map<String, Object> structureMap = new HashMap<>();

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
        this.fetchTime = record.fetchTime;
        this.parentURL = record.parentURL;
        this.fetchContentURL = record.fetchContentURL;
        this.status = record.status;
        this.userDefinedMap = record.userDefinedMap;
        this.structureMap = record.structureMap;
    }

    /**
     * 重新构建key
     */
    public void rebuildKey() {
        key = RandomID.build();
    }
}
