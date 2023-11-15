package xin.manong.darwin.parser.service;

import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

/**
 * 全局抽链服务
 *
 * @author frankcl
 * @date 2023-11-15 14:36:30
 */
public interface LinkFollowService {

    /**
     * 范围抽链
     *
     * @param request 解析请求
     * @return 解析响应
     */
    ParseResponse parse(ParseRequest request);
}
