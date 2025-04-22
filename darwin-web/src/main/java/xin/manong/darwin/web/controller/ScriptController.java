package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.script.CompileException;
import xin.manong.darwin.parser.script.ScriptFactory;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.response.CompileResult;
import xin.manong.darwin.spider.core.HTMLSpider;
import xin.manong.darwin.web.request.DebugRequest;
import xin.manong.darwin.web.response.*;

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

    @Resource
    protected HTMLSpider htmlSpider;
    @Resource
    protected ParseService parseService;

    /**
     * 编译脚本：检测脚本有效性
     *
     * @param request 编译请求
     * @return 编译结果
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("compile")
    @PostMapping("compile")
    public CompileResult compile(@RequestBody CompileRequest request) {
        if (request == null) throw new BadRequestException("脚本编译请求为空");
        request.check();
        return parseService.compile(request);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("debug")
    @PostMapping("debug")
    public DebugResponse debug(@RequestBody DebugRequest request) throws CompileException {
        if (request == null) throw new BadRequestException("脚本调试请求为空");
        request.check();
        try (Script script = ScriptFactory.make(request.scriptType, request.script)) {
            URLRecord record = htmlSpider.fetch(request.url);
            ParseRequestBuilder builder = new ParseRequestBuilder();
            builder.url(request.url).html(record.html);
            if (!StringUtils.isEmpty(record.redirectURL)) builder.redirectURL(record.redirectURL);
            ParseResponse parseResponse = script.doExecute(builder.build());
            if (!parseResponse.status) {
                logger.error("Parse failed for url:{}", request.url);
                DebugError debugError = new DebugError(parseResponse.message, null);
                debugError.debugLog = parseResponse.debugLog;
                return debugError;
            }
            DebugSuccess debugSuccess = new DebugSuccess(parseResponse.fieldMap,
                    parseResponse.children, parseResponse.userDefinedMap);
            debugSuccess.debugLog = parseResponse.debugLog;
            return debugSuccess;
        } catch (Exception e) {
            logger.error("Exception occurred when debugging url:{}", request.url);
            logger.error(e.getMessage(), e);
            return new DebugError(e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }
}
