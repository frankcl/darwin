package xin.manong.darwin.parser.service;

import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;

/**
 * 抽链服务
 *
 * @author frankcl
 * @date 2023-11-15 14:36:30
 */
public interface LinkExtractService {

    /**
     * 抽链
     *
     * @param request 解析请求
     * @return 解析响应
     */
    ParseResponse extract(ScriptParseRequest request);
}
