package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;

import java.io.Serial;

/**
 * @author frankcl
 * @date 2023-12-12 14:50:12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyUpdateRequest extends ProxyRequest {

    @Serial
    private static final long serialVersionUID = -5927703215374099983L;

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
        if (id == null) throw new BadRequestException("代理ID为空");
        if (StringUtils.isEmpty(address) && port == null && category == null &&
            StringUtils.isEmpty(username) && StringUtils.isEmpty(password) && expiredTime == null) {
            throw new BadRequestException("代理更新信息为空");
        }
        if (port != null && port <= 0) throw new BadRequestException("代理端口非法");
        if (category != null && !Constants.SUPPORT_PROXY_CATEGORIES.containsKey(category)) {
            throw new BadRequestException("不支持代理类型");
        }
    }
}
