package xin.manong.darwin.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.ws.rs.BadRequestException;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.request.PlanExecuteRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.jersey.WebResponse;

import java.nio.charset.StandardCharsets;

/**
 * darwin客户端
 *
 * @author frankcl
 * @date 2025-11-05 16:49:32
 */
public class DarwinClient {

    private static final Logger logger = LoggerFactory.getLogger(DarwinClient.class);

    private static final String API_SUBMIT_PLAN = "/api/plan/submit";

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
     * 提交计划
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    public boolean planSubmit(PlanExecuteRequest request) {
        if (request == null) throw new BadRequestException("请求为空");
        request.check();
        String requestURL = String.format("%s%s", config.serverURL, API_SUBMIT_PLAN);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL,
                RequestFormat.JSON, JSON.parseObject(JSON.toJSONString(request)));
        try (Response response = httpClient.execute(httpRequest)) {
            if (!response.isSuccessful()) {
                logger.error("Execute http request failed when submitting plan, code:{}",
                        response.code());
                return false;
            }
            ResponseBody responseBody = response.body();
            assert responseBody != null;
            String body = new String(responseBody.bytes(), StandardCharsets.UTF_8);
            WebResponse<Boolean> webResponse = JSON.parseObject(body, new TypeReference<>() {});
            if (!webResponse.status || !webResponse.data) {
                logger.error("Submit plan failed, http code:{}, message:{}, request id:{}",
                        webResponse.code, webResponse.message, webResponse.requestId);
            }
            return webResponse.status && webResponse.data;
        } catch (Exception e) {
            logger.error("Exception occurred when submitting plan", e);
            return false;
        }
    }
}
