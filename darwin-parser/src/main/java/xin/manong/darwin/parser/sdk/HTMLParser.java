package xin.manong.darwin.parser.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTML/JSON解析器
 * 用户自定义解析器需要继承此类，实现parse接口
 *
 * @author frankcl
 * @date 2023-03-16 15:07:13
 */
public abstract class HTMLParser {

    protected static final Logger logger = LoggerFactory.getLogger(HTMLParser.class);

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse parse(ParseRequest request);
}
