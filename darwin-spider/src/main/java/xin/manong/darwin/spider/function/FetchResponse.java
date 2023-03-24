package xin.manong.darwin.spider.function;

/**
 * 抓取响应
 *
 * @author frankcl
 * @date 2023-03-24 17:41:16
 */
public class FetchResponse {

    public boolean status;
    public String html;

    /**
     * 构建成功抓取响应
     *
     * @param html HTML
     * @return 成功响应
     */
    public static FetchResponse buildOK(String html) {
        FetchResponse response = new FetchResponse();
        response.status = true;
        response.html = html;
        return response;
    }

    /**
     * 构建失败抓取响应
     *
     * @return 失败抓取响应
     */
    public static FetchResponse buildError() {
        FetchResponse response = new FetchResponse();
        response.status = false;
        return response;
    }
}
