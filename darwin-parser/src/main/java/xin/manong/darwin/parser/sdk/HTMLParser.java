package xin.manong.darwin.parser.sdk;

import java.util.UUID;

/**
 * HTML/JSON解析器
 * 用户自定义解析器需要继承此类，实现parse接口
 *
 * @author frankcl
 * @date 2023-03-16 15:07:13
 */
public abstract class HTMLParser {

    protected final GroovyLogger logger = new GroovyLogger();

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public final ParseResponse execute(ParseRequest request) {
        try {
            String name = String.format("%s$%s", HTMLParser.class.getName(), UUID.randomUUID());
            logger.open(name);
            ParseResponse response = parse(request);
            String debugLog = logger.getLogContent();
            if (response != null && debugLog != null) response.debugLog = debugLog;
            return response;
        } finally {
            logger.close();
        }
    }

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse parse(ParseRequest request);
}
