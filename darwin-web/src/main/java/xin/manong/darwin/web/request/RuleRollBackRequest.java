package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;

/**
 * 规则回滚请求
 *
 * @author frankcl
 * @date 2024-01-05 11:50:06
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleRollBackRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RuleRollBackRequest.class);

    /**
     * 规则ID
     */
    @JsonProperty("rule_id")
    public Integer ruleId;

    /**
     * 规则历史ID
     */
    @JsonProperty("rule_history_id")
    public Integer ruleHistoryId;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (ruleId == null) {
            logger.error("rule id is null");
            throw new BadRequestException("规则ID为空");
        }
        if (ruleHistoryId == null) {
            logger.error("rule history id is null");
            throw new BadRequestException("规则历史ID为空");
        }
    }
}
