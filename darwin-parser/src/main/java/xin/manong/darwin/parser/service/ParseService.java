package xin.manong.darwin.parser.service;

import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.request.HTMLParseRequest;
import xin.manong.darwin.parser.service.response.CompileResponse;

/**
 * 解析服务接口
 *
 * @author frankcl
 * @date 2023-08-25 11:36:10
 */
public interface ParseService {

    /**
     * 解析HTML文本
     *
     * @param request HTML解析请求
     * @return 解析响应
     */
    ParseResponse parse(HTMLParseRequest request);

    /**
     * 编译脚本
     *
     * @param scriptType 脚本类型
     * @param scriptCode 脚本代码
     * @return 编译响应
     */
    CompileResponse compile(int scriptType, String scriptCode);
}
