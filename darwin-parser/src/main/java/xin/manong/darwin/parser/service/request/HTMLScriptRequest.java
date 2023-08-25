package xin.manong.darwin.parser.service.request;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.sdk.ParseRequest;

/**
 * HTML及脚本请求
 * 1. HTML及URL信息
 * 2. 解析脚本信息
 *
 * @author frankcl
 * @date 2023-03-16 15:15:08
 */
public class HTMLScriptRequest extends ParseRequest {

    private static final Logger logger = LoggerFactory.getLogger(HTMLScriptRequest.class);

    /**
     * 脚本类型
     */
    public int scriptType;
    /**
     * 脚本代码
     */
    public String scriptCode;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (!super.check()) return false;
        if (StringUtils.isEmpty(scriptCode)) {
            logger.error("script code is empty");
            return false;
        }
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("unsupported script type[{}]", scriptType);
            return false;
        }
        return true;
    }
}
