package xin.manong.darwin.parser.service;

import org.springframework.lang.NonNull;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.request.RuleParseRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.response.CompileResult;

import java.io.IOException;

/**
 * 解析服务接口
 *
 * @author frankcl
 * @date 2023-08-25 11:36:10
 */
public interface ParseService {

    /**
     * 解析文本
     *
     * @param request 脚本解析请求
     * @return 解析响应
     */
    ParseResponse parse(@NonNull ScriptParseRequest request);

    /**
     * 解析文本
     *
     * @param request 规则解析请求
     * @return 解析响应
     */
    ParseResponse parse(@NonNull RuleParseRequest request);

    /**
     * 编译脚本
     *
     * @param request 编译请求
     * @return 编译结果
     */
    CompileResult compile(@NonNull CompileRequest request);

    /**
     * 格式化Groovy脚本
     * 使用Groovy模板完善用户脚本
     *
     * @param script 用户脚本
     * @return 完整Groovy脚本
     */
    String formatGroovy(String script);

    /**
     * 获取代码模版
     *
     * @param scriptType 脚本类型
     * @return 代码模版
     * @throws IOException I/O异常
     */
    String scriptTemplate(int scriptType) throws IOException;
}
