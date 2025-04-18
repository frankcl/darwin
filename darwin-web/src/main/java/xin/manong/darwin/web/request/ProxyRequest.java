package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.base.util.CommonUtil;

import java.io.Serial;
import java.io.Serializable;

/**
 * 代理请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8440988806676915104L;

    /**
     * 代理地址
     */
    @JsonProperty("address")
    public String address;

    /**
     * 代理端口
     */
    @JsonProperty("port")
    public Integer port;

    /**
     * 代理类型
     * 长效代理：1
     * 短效代理：2
     */
    @JsonProperty("category")
    public Integer category;

    /**
     * 用户名
     */
    @JsonProperty("username")
    public String username;

    /**
     * 密码
     */
    @JsonProperty("password")
    public String password;

    /**
     * 过期时间
     */
    @JsonProperty("expiredTime")
    public Long expiredTime;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (!CommonUtil.isValidIP(address)) throw new BadRequestException("代理地址非法");
        if (port == null || port <= 0) throw new BadRequestException("代理端口非法");
        if (!Constants.SUPPORT_PROXY_CATEGORIES.containsKey(category)) throw new BadRequestException("不支持代理类型");
    }
}
