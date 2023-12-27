package xin.manong.darwin.parser.sdk;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.URLRecord;

import java.util.List;
import java.util.Map;

/**
 * 解析响应
 *
 * @author frankcl
 * @date 2023-03-16 15:15:22
 */
public class ParseResponse {

    /**
     * 解析状态
     */
    public boolean status;
    /**
     * 错误信息
     */
    public String message;
    /**
     * 结构化数据
     */
    public Map<String, Object> fieldMap;
    /**
     * 用户透传数据
     */
    public Map<String, Object> userDefinedMap;
    /**
     * 抽链列表
     */
    public List<URLRecord> childURLs;

    /**
     * 构建错误解析响应
     *
     * @param message 错误信息
     * @return 错误解析响应
     */
    public static ParseResponse buildError(String message) {
        ParseResponse response = new ParseResponse();
        response.status = false;
        response.message = StringUtils.isEmpty(message) ? "" : message;
        return response;
    }

    /**
     * 构建成功解析响应
     *
     * @param fieldMap 结构化数据
     * @param childURLs 抽链列表
     * @param userDefinedMap 用户自定义数据
     * @return 成功解析响应
     */
    public static ParseResponse buildOK(Map<String, Object> fieldMap,
                                        List<URLRecord> childURLs,
                                        Map<String, Object> userDefinedMap) {
        ParseResponse response = new ParseResponse();
        response.status = true;
        response.fieldMap = fieldMap;
        response.childURLs = childURLs;
        response.userDefinedMap = userDefinedMap;
        return response;
    }
}
