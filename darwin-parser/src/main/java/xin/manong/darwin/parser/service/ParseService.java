package xin.manong.darwin.parser.service;

import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.response.CompileResult;

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
     * @param request 脚本解析请求
     * @return 解析响应
     */
    ParseResponse parse(ScriptParseRequest request);

    /**
     * 编译脚本
     *
     * @param request 编译请求
     * @return 编译结果
     */
    CompileResult compile(CompileRequest request);
}
