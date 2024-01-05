package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.URLRecord;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 脚本调试响应
 *
 * @author frankcl
 * @date 2024-01-05 14:40:04
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebugResponse implements Serializable {

    /**
     * 是否成功
     */
    @JsonProperty("success")
    public boolean success;
    /**
     * 错误信息
     */
    @JsonProperty("message")
    public String message;
    /**
     * 调试日志
     */
    @JsonProperty("debug_log")
    public String debugLog;
    /**
     * 异常堆栈
     */
    @JsonProperty("stack_trace")
    public String stackTrace;
    /**
     * 结构化数据
     */
    @JsonProperty("field_map")
    public Map<String, Object> fieldMap;
    /**
     * 用户透传数据
     */
    @JsonProperty("user_defined_map")
    public Map<String, Object> userDefinedMap;
    /**
     * 抽链列表
     */
    @JsonProperty("child_urls")
    public List<URLRecord> childURLs;

    /**
     * 构建成功响应
     *
     * @param fieldMap 结构化字段
     * @param childURLs 子链列表
     * @param userDefinedMap 自定义字段
     * @return 成功响应
     */
    public static DebugResponse buildOK(Map<String, Object> fieldMap,
                                        List<URLRecord> childURLs,
                                        Map<String, Object> userDefinedMap) {
        DebugResponse response = new DebugResponse();
        response.success = true;
        response.fieldMap = fieldMap;
        response.childURLs = childURLs;
        response.userDefinedMap = userDefinedMap;
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param message 错误信息
     * @param stackTrace 异常堆栈
     * @return 失败响应
     */
    public static DebugResponse buildError(String message, String stackTrace) {
        DebugResponse response = new DebugResponse();
        response.success = false;
        response.message = StringUtils.isEmpty(message) ? "" : message;
        response.stackTrace = stackTrace;
        return response;
    }
}
