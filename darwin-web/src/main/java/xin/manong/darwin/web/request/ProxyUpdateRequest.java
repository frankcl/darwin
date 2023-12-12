package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import javax.ws.rs.BadRequestException;

/**
 * @author frankcl
 * @date 2023-12-12 14:50:12
 */
public class ProxyUpdateRequest extends ProxyRequest {

    private static final Logger logger = LoggerFactory.getLogger(ProxyUpdateRequest.class);

    /**
     * 代理ID
     */
    @JsonProperty("id")
    public Integer id;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (id == null) {
            logger.error("proxy id is empty");
            throw new BadRequestException("代理ID为空");
        }
        if (StringUtils.isEmpty(address) && port == null && category == null &&
            StringUtils.isEmpty(username) && StringUtils.isEmpty(password) && expiredTime == null) {
            logger.error("proxy update info is empty");
            throw new BadRequestException("代理更新信息为空");
        }
        if (port != null && port <= 0) {
            logger.error("proxy port[{}] is invalid", port);
            throw new BadRequestException(String.format("代理端口[%d]非法", port));
        }
        if (category != null && !Constants.SUPPORT_PROXY_CATEGORIES.containsKey(category)) {
            logger.error("unsupported proxy category[{}]", category);
            throw new BadRequestException(String.format("不支持代理类型[%d]", category));
        }
    }
}
