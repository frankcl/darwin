package xin.manong.darwin.runner.proxy;

import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;

import java.util.List;

/**
 * 代理获取
 *
 * @author frankcl
 * @date 2026-02-06 14:33:47
 */
public abstract class ProxyGetter {

    private static final Logger logger = LoggerFactory.getLogger(ProxyGetter.class);

    protected HttpClient httpClient;

    public ProxyGetter() {
        httpClient = new HttpClient();
    }

    /**
     * 初始化
     *
     * @param config 配置
     * @return 成功返回true，否则返回false
     */
    public boolean init(ProxyGetConfig config) {
        return true;
    }

    /**
     * 销毁
     */
    public void destroy() {
    }

    /**
     * 执行HTTP请求
     *
     * @param request HTTP请求
     * @return 成功返回响应文本，否则返回null
     */
    protected String executeHTTPRequest(HttpRequest request) {
        try (Response response = httpClient.execute(request)) {
            if (!response.isSuccessful()) {
                logger.error("Execute proxy request failed, http code:{} for {}",
                        response.code(), request.requestURL);
                return null;
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                logger.error("Execute proxy request failed, response body is null for {}", request.requestURL);
                return null;
            }
            String content = responseBody.string();
            if (StringUtils.isEmpty(content)) {
                logger.error("Execute proxy request failed, response body is empty for {}", request.requestURL);
                return null;
            }
            return content;
        } catch (Exception e) {
            logger.error("Execute proxy request exception for {}", request.requestURL);
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 批量获取代理
     *
     * @return 代理列表
     */
    public abstract List<Proxy> batchGet();
}
