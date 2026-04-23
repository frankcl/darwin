package xin.manong.darwin.spider.fetcher;

/**
 * 抓取器接口
 *
 * @author frankcl
 * @date 2026-04-22 17:00:19
 */
public interface Fetcher<T> {

    /**
     * 抓取数据
     *
     * @param request 请求
     * @return 响应
     */
    Response<T> fetch(Request request);
}
