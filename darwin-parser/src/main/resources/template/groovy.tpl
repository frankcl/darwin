import xin.manong.darwin.parser.sdk.HTMLParser;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

import java.util.HashMap;

/**
 * 支持Jsoup进行DOM解析
 *   Document document = Jsoup.parse(request.text, request.url);
 *
 * 支持fastjson进行JSON解析
 *   JSONObject response = JSONObject.parseObject(request.text);
 *
 * 可使用ParseResponse.buildOK()方法生成成功解析响应
 * 可使用ParseResponse.buildError()方法生成错误解析响应
 * 可通过如下方法打印日志(底层为slf4j实现，支持可变参数日志打印)
 *   getLogger().info("this is a test log: {}", message);
 */
public class GroovyParser extends HTMLParser {

    /**
     * 解析文本
     *
     * @param request 解析请求
     * @return 解析响应
     */
    @Override
    public ParseResponse parse(ParseRequest request) {
        /**
         * TODO 在这里编写解析逻辑
         */
        return null;
    }
}
