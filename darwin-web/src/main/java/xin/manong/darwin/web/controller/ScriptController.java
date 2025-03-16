package xin.manong.darwin.web.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.script.ScriptFactory;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.service.component.CharsetDetector;
import xin.manong.darwin.web.request.DebugRequest;
import xin.manong.darwin.web.response.DebugResponse;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;

import java.nio.charset.Charset;

/**
 * 脚本控制器
 *
 * @author frankcl
 * @date 2024-01-05 14:36:43
 */
@RestController
@Controller
@Path("/api/script")
@RequestMapping("/api/script")
public class ScriptController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptController.class);

    protected HttpClient httpClient = new HttpClient();

    /**
     * 编译脚本
     */
    private static class CompiledScript {
        public Script script;
        public Throwable throwable;
    }

    /**
     * HTML抓取结果
     */
    private static class HTMLContent {
        public String html;
        public String redirectURL;
        public Throwable throwable;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("debug")
    @PostMapping("debug")
    public DebugResponse debug(@RequestBody DebugRequest request) {
        if (request == null) throw new BadRequestException("脚本调试请求为空");
        request.check();
        CompiledScript compiledScript = compile(request.scriptType, request.script);
        if (compiledScript.throwable != null) {
            return DebugResponse.buildError(compiledScript.throwable.getMessage(),
                    ExceptionUtils.getStackTrace(compiledScript.throwable));
        }
        HTMLContent htmlContent = fetchHTML(request.url);
        if (htmlContent == null || htmlContent.throwable != null) {
            return DebugResponse.buildError("抓取URL失败",
                    htmlContent == null ? null : ExceptionUtils.getStackTrace(htmlContent.throwable));
        }
        ParseRequestBuilder builder = new ParseRequestBuilder();
        builder.url(request.url).html(htmlContent.html);
        if (!StringUtils.isEmpty(htmlContent.redirectURL)) builder.redirectURL(htmlContent.redirectURL);
        try {
            ParseResponse parseResponse = compiledScript.script.doExecute(builder.build());
            if (!parseResponse.status) {
                logger.error("parse failed for url[{}]", request.url);
                DebugResponse response = DebugResponse.buildError(String.format("解析抓取内容失败[%s]",
                        parseResponse.message), null);
                response.debugLog = parseResponse.debugLog;
                return response;
            }
            DebugResponse response = DebugResponse.buildOK(parseResponse.fieldMap,
                    parseResponse.childURLs, parseResponse.userDefinedMap);
            response.debugLog = parseResponse.debugLog;
            return response;
        } catch (Exception e) {
            logger.error("exception occurred when parsing url[{}]", request.url);
            logger.error(e.getMessage(), e);
            return DebugResponse.buildError(String.format("解析抓取内容异常[%s]", e.getMessage()),
                    ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                compiledScript.script.doClose();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 编译脚本
     *
     * @param scriptType 脚本类型
     * @param scriptCode 脚本代码
     * @return 编译结果
     */
    private CompiledScript compile(int scriptType, String scriptCode) {
        try {
            Script script = ScriptFactory.make(scriptType, scriptCode);
            CompiledScript compiledScript = new CompiledScript();
            compiledScript.script = script;
            return compiledScript;
        } catch (Exception e) {
            logger.error("compile script failed");
            logger.error(e.getMessage(), e);
            CompiledScript compiledScript = new CompiledScript();
            compiledScript.throwable = e;
            return compiledScript;
        }
    }

    /**
     * 抓取URL
     *
     * @param requestURL 请求URL
     * @return 抓取结果，失败返回null
     */
    private HTMLContent fetchHTML(String requestURL) {
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, null);
        try (Response httpResponse = httpClient.execute(httpRequest)) {
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                logger.error("fetch failed for url[{}], http code[{}]",
                        requestURL, httpResponse == null ? -1 : httpResponse.code());
                return null;
            }
            assert httpResponse.body() != null;
            byte[] body = httpResponse.body().bytes();
            String charset = CharsetDetector.detect(body, body.length);
            String html = new String(body, Charset.forName(charset));
            String targetURL = httpResponse.request().url().url().toString();
            HTMLContent htmlContent = new HTMLContent();
            htmlContent.html = html;
            if (!StringUtils.isEmpty(targetURL) && !targetURL.equals(requestURL)) {
                htmlContent.redirectURL = targetURL;
            }
            return htmlContent;
        } catch (Exception e) {
            logger.error("exception occurred when fetch url[{}]", requestURL);
            logger.error(e.getMessage(), e);
            HTMLContent htmlContent = new HTMLContent();
            htmlContent.throwable = e;
            return htmlContent;
        }
    }
}
