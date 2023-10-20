package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 应用用户关系请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUserRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AppUserRequest.class);

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Long appId;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
    /**
     * 用户真实姓名
     */
    @JsonProperty("real_name")
    public String realName;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (appId == null) {
            logger.error("app id is null");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(userId)) {
            logger.error("user id is empty");
            throw new BadRequestException("用户名为空");
        }
        if (StringUtils.isEmpty(realName)) {
            logger.error("user real name is empty");
            throw new BadRequestException("用户真实姓名为空");
        }
    }
}
