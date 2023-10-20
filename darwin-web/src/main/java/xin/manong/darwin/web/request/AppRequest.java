package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 应用请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AppRequest.class);

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
        if (StringUtils.isEmpty(name)) {
            logger.error("app name is empty");
            throw new BadRequestException("应用名为空");
        }
    }
}
