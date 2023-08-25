package xin.manong.darwin.parser.sdk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * 链接URL
     */
    public String url;
    /**
     * 重定向URL
     */
    public String redirectURL;
    /**
     * 网页内容HTML
     */
    public String html;
    /**
     * 用户透传数据
     */
    public Map<String, Object> userDefinedMap;

    /**
     * 检测有效性
     * 1. 解析URL不能为空
     * 2. 解析HTML不能为空
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            return false;
        }
        if (StringUtils.isEmpty(html)) {
            logger.error("html is empty");
            return false;
        }
        return true;
    }
}
