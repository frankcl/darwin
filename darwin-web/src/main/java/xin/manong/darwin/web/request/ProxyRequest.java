package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 代理请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ProxyRequest.class);

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
        if (StringUtils.isEmpty(address)) {
            logger.error("proxy address is empty");
            throw new BadRequestException("代理地址为空");
        }
        if (port == null || port <= 0) {
            logger.error("proxy port[{}] is invalid", port);
            throw new BadRequestException(String.format("代理端口[%d]非法", port == null ? -1 : port));
        }
        if (!Constants.SUPPORT_PROXY_CATEGORIES.containsKey(category)) {
            logger.error("unsupported proxy category[{}]", category);
            throw new BadRequestException(String.format("不支持代理类型[%d]", category == null ? -1 : category));
        }
    }
}
