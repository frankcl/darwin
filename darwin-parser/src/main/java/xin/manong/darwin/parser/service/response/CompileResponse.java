package xin.manong.darwin.parser.service.response;

import org.apache.commons.lang3.StringUtils;

/**
 * 编译响应
 *
 * @author frankcl
 * @date 2023-08-25 11:38:39
 */
public class CompileResponse {

    public boolean status;
    public String message;

    /**
     * 构建编译成功响应
     *
     * @return 编译成功响应
     */
    public static CompileResponse buildOK() {
        CompileResponse response = new CompileResponse();
        response.status = true;
        return response;
    }

    /**
     * 构建编译失败相应
     *
     * @param message 错误信息
     * @return 编译失败相应
     */
    public static CompileResponse buildError(String message) {
        CompileResponse response = new CompileResponse();
        response.status = false;
        response.message = StringUtils.isEmpty(message) ? "" : message;
        return response;
    }
}
