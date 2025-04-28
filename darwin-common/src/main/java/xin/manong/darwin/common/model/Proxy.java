package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
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

/**
 * 代理IP
 *
 * @author frankcl
 * @date 2023-12-11 11:42:35
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "proxy", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proxy extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);

    /**
     * 代理ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 代理地址
     */
    @TableField(value = "address")
    @JSONField(name = "address")
    @JsonProperty("address")
    public String address;

    /**
     * 代理端口
     */
    @TableField(value = "port")
    @JSONField(name = "port")
    @JsonProperty("port")
    public Integer port;

    /**
     * 代理类型
     */
    @TableField(value = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * 用户名
     */
    @TableField(value = "username")
    @JSONField(name = "username")
    @JsonProperty("username")
    public String username;

    /**
     * 密码
     */
    @TableField(value = "password")
    @JSONField(name = "password")
    @JsonProperty("password")
    public String password;

    /**
     * 过期时间
     */
    @TableField(value = "expired_time")
    @JSONField(name = "expired_time")
    @JsonProperty("expired_time")
    public Long expiredTime;

    /**
     * 判断代理是否过期
     * 1. expiredTime小于等于0，不过期
     * 2. expiredTime小于等于当前时间则过期
     *
     * @return 过期返回true，否则返回false
     */
    public boolean isExpired() {
        if (expiredTime == null || expiredTime <= 0) return false;
        return System.currentTimeMillis() >= expiredTime;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("https://");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            buffer.append(username).append(":").append(password).append("@");
        }
        buffer.append(address).append(":").append(port);
        return buffer.toString();
    }

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(address)) {
            logger.error("Proxy address is empty");
            return false;
        }
        if (port == null || port <= 0) {
            logger.error("Proxy port:{} is invalid", port == null ? - 1 : port);
            return false;
        }
        if (!Constants.SUPPORT_PROXY_CATEGORIES.containsKey(category)) {
            logger.error("Unsupported proxy category:{}", category);
            return false;
        }
        return true;
    }
}
