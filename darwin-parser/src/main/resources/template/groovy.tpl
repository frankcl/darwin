import xin.manong.darwin.parser.sdk.HTMLParser;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

import java.util.HashMap;

public class GroovyParser extends HTMLParser {

    /**
     * 解析文本：支持Jsoup进行DOM解析，支持fastjson进行JSON解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    @Override
    public ParseResponse parse(ParseRequest request) {
        /**
         * TODO 在这里编写解析逻辑
         *
         * 可使用ParseResponse.buildOK()方法生成成功解析响应
         * 可使用ParseResponse.buildError()方法生成错误解析响应
         */
        return null;
    }
}
