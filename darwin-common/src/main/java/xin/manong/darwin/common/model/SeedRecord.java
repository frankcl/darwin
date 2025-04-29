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
import xin.manong.darwin.common.model.handler.JSONObjectMapHandler;
import xin.manong.darwin.common.model.json.MapDeserializer;
import xin.manong.weapon.aliyun.ots.annotation.Column;
import xin.manong.weapon.aliyun.ots.annotation.PrimaryKey;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.DomainUtil;
import xin.manong.weapon.base.util.RandomID;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 种子URL
 *
 * @author frankcl
 * @date 2025-04-01 14:28:13
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "seed", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeedRecord extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(SeedRecord.class);

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
     * 超时时间（毫秒）
     */
    @TableField(value = "timeout")
    @Column(name = "timeout")
    @JSONField(name = "timeout")
    @JsonProperty("timeout")
    public Integer timeout;

    /**
     * 优先级
     * 高优先级：0
     * 正常优先级：1
     * 低优先级：2
     */
    @TableField(value = "priority")
    @Column(name = "priority")
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 抓取方式
     * 本地IP：0
     * 长效代理：1
     * 短效代理：2
     * 浏览器渲染：3
     */
    @TableField(value = "fetch_method")
    @Column(name = "fetch_method")
    @JSONField(name = "fetch_method")
    @JsonProperty("fetch_method")
    public Integer fetchMethod;

    /**
     * 抽链范围
     * 所有：1
     * 域domain：2
     * 站点host：3
     */
    @TableField(value = "link_scope")
    @Column(name = "link_scope")
    @JSONField(name = "link_scope")
    @JsonProperty("link_scope")
    public Integer linkScope;

    /**
     * URL
     */
    @TableField(value = "url")
    @Column(name = "url")
    @JSONField(name = "url")
    @JsonProperty("url")
    public String url;

    /**
     * 所属计划ID
     */
    @TableField(value = "plan_id")
    @Column(name = "plan_id")
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 允许分发
     */
    @TableField(value = "allow_dispatch")
    @Column(name = "allow_dispatch")
    @JSONField(name = "allow_dispatch")
    @JsonProperty("allow_dispatch")
    public Boolean allowDispatch;

    /**
     * 是否对URL进行正规化，默认进行normalize
     */
    @TableField(value = "normalize")
    @Column(name = "normalize")
    @JSONField(name = "normalize")
    @JsonProperty("normalize")
    public Boolean normalize;

    /**
     * host
     */
    @TableField(value = "host")
    @Column(name = "host")
    @JSONField(name = "host")
    @JsonProperty("host")
    public String host;

    /**
     * domain
     */
    @TableField(value = "domain")
    @Column(name = "domain")
    @JSONField(name = "domain")
    @JsonProperty("domain")
    public String domain;

    /**
     * HTTP header信息
     */
    @TableField(value = "headers", typeHandler = JSONObjectMapHandler.class)
    @Column(name = "headers")
    @JSONField(name = "headers", deserializeUsing = MapDeserializer.class)
    @JsonProperty("headers")
    public Map<String, String> headers = new HashMap<>();

    /**
     * 用户自定义字段，用于透传数据
     */
    @TableField(value = "custom_map", typeHandler = JSONObjectMapHandler.class)
    @Column(name = "custom_map")
    @JSONField(name = "custom_map", deserializeUsing = MapDeserializer.class)
    @JsonProperty("custom_map")
    public Map<String, Object> customMap = new HashMap<>();

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(key)) {
            logger.error("Key is empty");
            return false;
        }
        if (StringUtils.isEmpty(hash)) {
            logger.error("Hash is empty");
            return false;
        }
        if (StringUtils.isEmpty(url)) {
            logger.error("Url is empty");
            return false;
        }
        if (StringUtils.isEmpty(planId)) {
            logger.error("Plan id is empty");
            return false;
        }
        if (StringUtils.isEmpty(host)) host = CommonUtil.getHost(url);
        if (StringUtils.isEmpty(domain)) domain = DomainUtil.getDomain(host);
        if (allowDispatch == null) allowDispatch = true;
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        if (fetchMethod == null) fetchMethod = Constants.FETCH_METHOD_COMMON;
        if (!Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("Not support fetch method:{}", fetchMethod);
            return false;
        }
        if (linkScope != null && !Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope)) {
            logger.error("Not support link scope:{}", linkScope);
            return false;
        }
        if (priority > Constants.PRIORITY_LOW || priority < Constants.PRIORITY_HIGH) {
            logger.error("Not support priority:{}", priority);
            return false;
        }
        return true;
    }

    public SeedRecord() {
        key = RandomID.build();
        allowDispatch = true;
        createTime = System.currentTimeMillis();
    }

    public SeedRecord(String url) {
        this();
        this.url = url;
        hash = DigestUtils.md5Hex(url);
        host = CommonUtil.getHost(url);
        domain = DomainUtil.getDomain(host);
    }

    public SeedRecord(SeedRecord record) {
        key = record.key;
        url = record.url;
        host = record.host;
        domain = record.domain;
        hash = record.hash;
        planId = record.planId;
        createTime = record.createTime;
        updateTime = record.updateTime;
        timeout = record.timeout;
        fetchMethod = record.fetchMethod;
        priority = record.priority;
        linkScope = record.linkScope;
        allowDispatch = record.allowDispatch;
        normalize = record.normalize;
        customMap = record.customMap == null ? new HashMap<>() : new HashMap<>(record.customMap);
        headers = record.headers == null ? new HashMap<>() : new HashMap<>(record.headers);
    }

    /**
     * 是否使用代理抓取
     *
     * @return 使用代理返回true，否则返回false
     */
    public boolean isUseProxy() {
        return fetchMethod != null && (fetchMethod == Constants.FETCH_METHOD_LONG_PROXY ||
                fetchMethod == Constants.FETCH_METHOD_SHORT_PROXY);
    }

    /**
     * 是否范围抽链
     *
     * @return 范围抽链返回true，否则返回false
     */
    public boolean isScopeExtract() {
        return linkScope != null && Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope);
    }

    /**
     * 设置URL
     *
     * @param url URL
     */
    public void setUrl(String url) {
        this.url = url;
        this.hash = DigestUtils.md5Hex(this.url);
        this.host = CommonUtil.getHost(this.url);
        this.domain = DomainUtil.getDomain(host);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SeedRecord other)) return false;
        if (object == this) return true;
        return Objects.equals(key, other.key);
    }

    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }

    /**
     * 是否需要进行normalize
     *
     * @return 需要返回true，否则返回false
     */
    public boolean mustNormalize() {
        return normalize == null || normalize;
    }
}
