package xin.manong.darwin.parser.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 编译结果
 *
 * @author frankcl
 * @date 2025-04-10 11:38:39
 */
public class CompileResult {

    /**
     * 编译结果
     */
    @JsonProperty("status")
    public boolean status;
    /**
     * 错误信息
     */
    @JsonProperty("message")
    public String message;
    /**
     * 异常堆栈
     */
    @JsonProperty("stack_trace")
    public String stackTrace;
    /**
     * 标准输出
     */
    @JsonProperty("stdout")
    public String stdout;
    /**
     * 标准出错
     */
    @JsonProperty("stderr")
    public String stderr;

    public CompileResult(boolean status) {
        this.status = status;
    }

    /**
     * 构建编译成功结果
     *
     * @return 编译成功结果
     */
    public static CompileResult success() {
        return new CompileResult(true);
    }

    /**
     * 构建编译错误结果
     *
     * @param message 错误信息
     * @param stackTrace 异常堆栈
     * @return 编译错误结果
     */
    public static CompileResult error(String message, String stackTrace) {
        CompileResult response = new CompileResult(false);
        response.message = message;
        response.stackTrace = stackTrace;
        return response;
    }

    /**
     * 构建编译错误结果
     *
     * @param message 错误信息
     * @return 编译错误结果
     */
    public static CompileResult error(String message) {
        return error(message, null);
    }
}
