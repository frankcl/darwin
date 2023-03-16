package xin.manong.darwin.parse.parser;

import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * 脚本解析器
 * 用户自定义脚本解析器需要集成此类，实现parse接口
 *
 * @author frankcl
 * @date 2023-03-16 15:07:13
 */
public abstract class Parser {

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse parse(ParseRequest request);
}
