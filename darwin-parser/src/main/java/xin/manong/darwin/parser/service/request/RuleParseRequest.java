package xin.manong.darwin.parser.service.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.sdk.ParseRequest;

/**
 * 规则解析请求
 * 1. HTML及URL信息
 * 2. 解析规则信息
 *
 * @author frankcl
 * @date 2023-03-16 15:15:08
 */
public class RuleParseRequest extends ParseRequest {

    private static final Logger logger = LoggerFactory.getLogger(RuleParseRequest.class);

    /**
     * 规则ID
     */
    public Integer ruleId;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (!super.check()) return false;
        if (isScopeExtract()) return true;
        if (ruleId == null || ruleId <= 0) {
            logger.error("Invalid rule id");
            return false;
        }
        return true;
    }
}
