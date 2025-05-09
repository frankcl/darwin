package xin.manong.darwin.parser.sdk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import java.util.Map;

/**
 * 解析请求
 *
 * @author frankcl
 * @date 2023-03-16 15:15:08
 */
public class ParseRequest {

    private static final Logger logger = LoggerFactory.getLogger(ParseRequest.class);

    /**
     * 全局抽链范围
     * 所有all：1
     * 域domain：2
     * 站点host：3
     */
    public int linkScope;
    /**
     * 链接URL
     */
    public String url;
    /**
     * 重定向URL
     */
    public String redirectURL;
    /**
     * 文本内容
     */
    public String text;
    /**
     * 用户透传数据
     */
    public Map<String, Object> customMap;

    /**
     * 检测有效性
     * 1. 解析URL不能为空
     * 2. 解析文本不能为空
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("Url is empty");
            return false;
        }
        if (StringUtils.isEmpty(text)) {
            logger.error("Text is empty");
            return false;
        }
        return true;
    }

    /**
     * 是否进行范围抽链，满足以下条件为范围抽链
     * 抽链范围合法
     *
     * @return 范围抽链返回true，否则返回false
     */
    public boolean isScopeExtract() {
        return Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope);
    }
}
