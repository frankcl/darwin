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

    @Serial
    private static final long serialVersionUID = 7563911493400497697L;

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
     * 抓取URL类型
     * 内容页：0
     * 列表页：1
     * 图片视频资源：2
     * 视频流：3
     */
    @TableField(value = "category")
    @Column(name = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * 抓取并发级别
     * domain:0
     * host:1
     */
    @TableField(value = "concurrent_level")
    @Column(name = "concurrent_level")
    @JSONField(name = "concurrent_level")
    @JsonProperty("concurrent_level")
    public Integer concurrentLevel;

    /**
     * 全局抽链范围
     * 所有：1
     * 域domain：2
     * 站点host：3
     */
    @TableField(value = "scope")
    @Column(name = "scope")
    @JSONField(name = "scope")
    @JsonProperty("scope")
    public Integer scope;

    @TableField(value = "url")
    @Column(name = "url")
    @JSONField(name = "url")
    @JsonProperty("url")
    public String url;

    /**
     * 计划ID
     */
    @TableField(value = "plan_id")
    @Column(name = "plan_id")
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * HTTP header信息
     */
    @TableField(value = "headers", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "headers")
    @JSONField(name = "headers", deserializeUsing = MapDeserializer.class)
    @JsonProperty("headers")
    public Map<String, String> headers = new HashMap<>();

    /**
     * 用户自定义字段，用于透传数据
     */
    @TableField(value = "user_defined_map", typeHandler = JSONMapObjectTypeHandler.class)
    @Column(name = "user_defined_map")
    @JSONField(name = "user_defined_map", deserializeUsing = MapDeserializer.class)
    @JsonProperty("user_defined_map")
    public Map<String, Object> userDefinedMap = new HashMap<>();

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
        if (StringUtils.isEmpty(hash)) {
            logger.error("hash is empty");
            return false;
        }
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            return false;
        }
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            return false;
        }
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        if (concurrentLevel == null) concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
        if (fetchMethod == null) fetchMethod = Constants.FETCH_METHOD_COMMON;
        if (!Constants.SUPPORT_CONTENT_CATEGORIES.containsKey(category)) {
            logger.error("not support URL category[{}]", category);
            return false;
        }
        if (!Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("not support fetch method[{}]", fetchMethod);
            return false;
        }
        if (!Constants.SUPPORT_CONCURRENT_LEVELS.containsKey(concurrentLevel)) {
            logger.error("not support concurrent level[{}]", concurrentLevel);
            return false;
        }
        if (priority > Constants.PRIORITY_LOW || priority < Constants.PRIORITY_HIGH) {
            logger.error("not support priority[{}]", priority);
            return false;
        }
        return true;
    }

    public SeedRecord() {
        key = RandomID.build();
        createTime = System.currentTimeMillis();
    }

    public SeedRecord(String url) {
        this();
        this.url = url;
        hash = DigestUtils.md5Hex(url);
    }

    public SeedRecord(SeedRecord record) {
        key = record.key;
        url = record.url;
        hash = record.hash;
        planId = record.planId;
        createTime = record.createTime;
        updateTime = record.updateTime;
        concurrentLevel = record.concurrentLevel;
        timeout = record.timeout;
        fetchMethod = record.fetchMethod;
        priority = record.priority;
        category = record.category;
        scope = record.scope;
        userDefinedMap = record.userDefinedMap == null ? new HashMap<>() : new HashMap<>(record.userDefinedMap);
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
     * 设置URL
     *
     * @param url URL
     */
    public void setUrl(String url) {
        this.url = url;
        this.hash = DigestUtils.md5Hex(url);
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
}
