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
 * 脚本调试请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebugRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DebugRequest.class);

    /**
     * 调试URL
     */
    @JsonProperty("url")
    public String url;

    /**
     * 调试脚本
     */
    @JsonProperty("script")
    public String script;

    /**
     * 调试脚本类型
     * 1：Groovy脚本
     * 2：JavaScript脚本
     */
    @JsonProperty("script_type")
    public Integer scriptType;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            throw new BadRequestException("URL为空");
        }
        if (StringUtils.isEmpty(script)) {
            logger.error("script is empty");
            throw new BadRequestException("调试脚本为空");
        }
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("not support script type[{}]", scriptType);
            throw new BadRequestException(String.format("不支持的脚本类型[%d]", scriptType));
        }
    }
}
