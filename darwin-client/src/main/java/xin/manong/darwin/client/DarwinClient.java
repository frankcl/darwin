package xin.manong.darwin.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.ws.rs.BadRequestException;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.request.AuthenticateRequest;
import xin.manong.darwin.common.request.PlanExecuteRequest;
import xin.manong.darwin.common.request.SeedRequest;
import xin.manong.darwin.common.request.SetCookieRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.base.http.RequestMethod;
import xin.manong.weapon.jersey.WebResponse;

/**
 * darwin客户端
 *
 * @author frankcl
 * @date 2025-11-05 16:49:32
 */
public class DarwinClient {

    private static final Logger logger = LoggerFactory.getLogger(DarwinClient.class);

    private static final String API_SUBMIT_PLAN = "/api/auth/plan/submit";
    private static final String API_ADD_SEED = "/api/auth/seed/add";
    private static final String API_SET_COOKIE = "/api/auth/cookie/set";

    private final DarwinClientConfig config;
    private final HttpClient httpClient;

    public DarwinClient(DarwinClientConfig config) {
        if (config == null || !config.check()) {
            logger.error("Darwin client config is null or invalid");
            throw new IllegalArgumentException("客户端配置非法");
        }
        this.config = config;
        this.httpClient = new HttpClient();
    }

    /**
     * 设置Cookie
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    public boolean setCookie(SetCookieRequest request) {
        if (request == null) throw new BadRequestException("设置Cookie请求为空");
        ((AuthenticateRequest) request).check();
        request.check();
        String requestURL = String.format("%s%s", config.serverURL, API_SET_COOKIE);
        HttpRequest httpRequest = new HttpRequest.Builder().requestURL(requestURL).method(RequestMethod.POST).
                format(RequestFormat.JSON).params(JSON.parseObject(JSON.toJSONString(request))).build();
        Boolean success = executeHttpRequest(httpRequest, Boolean.class);
        if (success == null || !success) logger.error("Set cookie failed for key:{}", request.key);
        return success != null && success;
    }

    /**
     * 添加种子
     *
     * @param request 添加种子请求
     * @return 成功返回true，否则返回false
     */
    public boolean addSeed(SeedRequest request) {
        if (request == null) throw new BadRequestException("添加种子请求为空");
        ((AuthenticateRequest) request).check();
        request.check();
        String requestURL = String.format("%s%s", config.serverURL, API_ADD_SEED);
        HttpRequest httpRequest = new HttpRequest.Builder().requestURL(requestURL).method(RequestMethod.PUT).
                format(RequestFormat.JSON).params(JSON.parseObject(JSON.toJSONString(request))).build();
        Boolean success = executeHttpRequest(httpRequest, Boolean.class);
        if (success == null || !success) logger.error("Add seed failed");
        return success != null && success;
    }

    /**
     * 提交计划
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    public boolean planSubmit(PlanExecuteRequest request) {
        if (request == null) throw new BadRequestException("提交计划请求为空");
        request.check();
        String requestURL = String.format("%s%s", config.serverURL, API_SUBMIT_PLAN);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL,
                RequestFormat.JSON, JSON.parseObject(JSON.toJSONString(request)));
        Boolean success = executeHttpRequest(httpRequest, Boolean.class);
        if (success == null || !success) logger.error("Submit plan failed for plan:{}", request.planId);
        return success != null && success;
    }

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @param recordType 响应数据类型
     * @return 成功返回响应数据，否则返回null
     */
    public <T> T executeHttpRequest(HttpRequest httpRequest, Class<T> recordType) {
        try {
            String body = executeHttpRequest(httpRequest);
            if (body == null) return null;
            WebResponse<T> response = JSON.parseObject(body, new TypeReference<>(recordType) {});
            if (response == null) return null;
            if (!response.status) {
                logger.error("Execute http request failed for http code:{}, cause:{}",
                        response.code, response.message);
                return null;
            }
            return response.data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @return 成功返回结果，否则返回null
     */
    private String executeHttpRequest(HttpRequest httpRequest) {
        try (Response httpResponse = httpClient.execute(httpRequest)) {
            if (failHttpResponse(httpRequest, httpResponse)) return null;
            assert httpResponse.body() != null;
            return httpResponse.body().string();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检测HTTP响应是否失败
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 失败返回true，否则返回false
     */
    private boolean failHttpResponse(HttpRequest httpRequest, Response httpResponse) {
        if (!httpResponse.isSuccessful() || httpResponse.code() != 200) {
            logger.error("Execute http request failed for url:{}, http code:{}",
                    httpRequest.requestURL, httpResponse.code());
            return true;
        }
        return false;
    }
}
