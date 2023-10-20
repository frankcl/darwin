package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 应用更新请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUpdateRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AppUpdateRequest.class);

    /**
     * 应用ID
     */
    @JsonProperty("id")
    public Long id;
    /**
     * 应用名
     */
    @JsonProperty("name")
    public String name;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (id == null) {
            logger.error("app id is null");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("update app info is empty");
            throw new BadRequestException("更新应用信息为空");
        }
    }
}
