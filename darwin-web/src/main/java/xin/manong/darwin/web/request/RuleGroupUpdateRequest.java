package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 规则分组更新请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleGroupUpdateRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RuleGroupUpdateRequest.class);

    /**
     * 规则分组ID
     */
    @JsonProperty("id")
    public Long id;
    /**
     * 规则分组名
     */
    @JsonProperty("name")
    public String name;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (id == null) {
            logger.error("rule group id is null");
            throw new BadRequestException("规则分组ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("rule group update info is empty");
            throw new BadRequestException("规则分组更新信息为空");
        }
    }
}
