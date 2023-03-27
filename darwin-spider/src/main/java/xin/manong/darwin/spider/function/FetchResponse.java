package xin.manong.darwin.spider.function;

/**
 * 抓取响应
 *
 * @author frankcl
 * @date 2023-03-24 17:41:16
 */
public class FetchResponse {

    public boolean status;
    public int httpCode;
    public String message;
    public String html;

    /**
     * 构建成功抓取响应
     *
     * @param httpCode HTTP状态码
     * @param html HTML
     * @return 成功响应
     */
    public static FetchResponse buildOK(int httpCode, String html) {
        FetchResponse response = new FetchResponse();
        response.status = true;
        response.httpCode = httpCode;
        response.html = html;
        return response;
    }

    /**
     * 构建失败抓取响应
     *
     * @param httpCode HTTP状态码
     * @param message 错误信息
     * @return 失败抓取响应
     */
    public static FetchResponse buildError(int httpCode, String message) {
        FetchResponse response = new FetchResponse();
        response.status = false;
        response.httpCode = httpCode;
        response.message = message;
        return response;
    }
}
