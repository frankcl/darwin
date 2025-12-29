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
     * 添加种子
     *
     * @param request 添加种子请求
     * @return 成功返回true，否则返回false
     */
    public boolean addSeed(SeedRequest request) {
        if (request == null) throw new BadRequestException("添加种子为空");
        ((AuthenticateRequest) request).check();
        request.check();
        String requestURL = String.format("%s%s", config.serverURL, API_ADD_SEED);
        HttpRequest httpRequest = new HttpRequest.Builder().requestURL(requestURL).method(RequestMethod.PUT).
                format(RequestFormat.JSON).params(JSON.parseObject(JSON.toJSONString(request))).build();
        WebResponse<Boolean> webResponse = execute(httpRequest, Boolean.class);
        if (webResponse == null) return false;
        if (!webResponse.status || !webResponse.data) {
            logger.error("Add seed failed, http code:{}, message:{}, request id:{}",
                    webResponse.code, webResponse.message, webResponse.requestId);
        }
        return webResponse.status && webResponse.data;
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
        WebResponse<Boolean> webResponse = execute(httpRequest, Boolean.class);
        if (webResponse == null) return false;
        if (!webResponse.status || !webResponse.data) {
            logger.error("Submit plan failed, http code:{}, message:{}, request id:{}",
                    webResponse.code, webResponse.message, webResponse.requestId);
        }
        return webResponse.status && webResponse.data;
    }

    /**
     * 执行HTTP请求，返回结构化结果
     *
     * @param httpRequest HTTP请求
     * @param recordType 数据类型
     * @return 成功返回结构化结果，否则返回null
     */
    public <T> WebResponse<T> execute(HttpRequest httpRequest, Class<T> recordType) {
        String body = execute(httpRequest);
        if (body == null) return null;
        try {
            return JSON.parseObject(body, new TypeReference<>(recordType) {});
        } catch (Exception e) {
            logger.error("Unexpected http response:{} for {}", body, recordType.getName());
            return null;
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param httpRequest HTTP请求
     * @return 成功返回结果，否则返回null
     */
    private String execute(HttpRequest httpRequest) {
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
