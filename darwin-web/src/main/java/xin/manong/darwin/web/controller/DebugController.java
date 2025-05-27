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
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.SeedRecord;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.script.ScriptFactory;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.LinkExtractService;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequestBuilder;
import xin.manong.darwin.parser.service.response.CompileResult;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.iface.SeedService;
import xin.manong.darwin.spider.core.TextSpider;
import xin.manong.darwin.web.request.DebugRequest;
import xin.manong.darwin.web.response.*;

import java.util.List;
import java.util.Objects;

/**
 * 调试控制器
 * 1. 种子调试
 * 2. 脚本调试
 *
 * @author frankcl
 * @date 2024-01-05 14:36:43
 */
@RestController
@Controller
@Path("/api/debug")
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Resource
    private TextSpider textSpider;
    @Resource
    private RuleService ruleService;
    @Resource
    private SeedService seedService;
    @Resource
    private ScriptFactory scriptFactory;
    @Resource
    private LinkExtractService linkExtractService;
    @Resource
    private ParseService parseService;

    /**
     * 编译脚本：检测脚本有效性
     *
     * @param request 编译请求
     * @return 编译结果
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("compileScript")
    @PostMapping("compileScript")
    public CompileResult compileScript(@RequestBody CompileRequest request) {
        if (request == null) throw new BadRequestException("脚本编译请求为空");
        request.check();
        return parseService.compile(request);
    }

    /**
     * 调试URL
     *
     * @param key 种子key
     * @param planId 计划ID
     * @return 调试结果
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("debugURL")
    @PostMapping("debugURL")
    public DebugResponse debugURL(@QueryParam("key") String key,
                                  @QueryParam("plan_id") String planId) throws Exception {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("调试种子key为空");
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("调试计划ID为空");
        SeedRecord seed = seedService.get(key);
        if (seed == null) throw new NotFoundException("种子不存在");
        try {
            Rule rule = getMatchedRule(seed, planId);
            URLRecord record = Converter.convert(seed);
            return debug(record, rule == null ? null : rule.scriptType, rule == null ? null : rule.script);
        } catch (Exception e) {
            return new DebugError(e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 调试脚本
     *
     * @param request 调试请求
     * @return 调试结果
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("debugScript")
    @PostMapping("debugScript")
    public DebugResponse debugScript(@RequestBody DebugRequest request) {
        if (request == null) throw new BadRequestException("脚本调试请求为空");
        request.check();
        return debug(new URLRecord(request.url), request.scriptType, request.script);
    }

    /**
     * 调试
     *
     * @param record 调试数据
     * @param scriptType 脚本类型
     * @param script 调试脚本
     * @return 调试结果
     */
    private DebugResponse debug(URLRecord record, Integer scriptType, String script) {
        try {
            textSpider.fetch(record);
            ParseResponse parseResponse = record.isScopeExtract() ?
                    extractLinks(record) : parse(record, scriptType, script);
            if (!parseResponse.status) {
                logger.error("Parse failed for url:{}", record.url);
                DebugError debugError = new DebugError(parseResponse.message, null);
                debugError.debugLog = parseResponse.debugLog;
                debugError.stdout = parseResponse.stdout;
                debugError.stderr = parseResponse.stderr;
                return debugError;
            }
            DebugSuccess debugSuccess = new DebugSuccess(parseResponse.fieldMap,
                    parseResponse.children, parseResponse.customMap);
            debugSuccess.debugLog = parseResponse.debugLog;
            debugSuccess.stdout = parseResponse.stdout;
            debugSuccess.stderr = parseResponse.stderr;
            return debugSuccess;
        } catch (Exception e) {
            logger.error("Exception occurred when debugging url:{}", record.url);
            logger.error(e.getMessage(), e);
            return new DebugError(e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 范围抽链
     *
     * @param record 数据
     * @return 解析结果
     */
    private ParseResponse extractLinks(URLRecord record) {
        ParseRequestBuilder builder = new ParseRequestBuilder();
        builder.url(record.url).text(record.text).linkScope(record.linkScope).
                customMap(record.customMap).redirectURL(record.redirectURL);
        return linkExtractService.extract(builder.build());
    }

    /**
     * 解析内容
     *
     * @param record 数据
     * @param scriptType 脚本类型
     * @param scriptCode 脚本代码
     * @return 解析结果
     */
    private ParseResponse parse(URLRecord record, int scriptType, String scriptCode) {
        Script script = null;
        try {
            script = scriptFactory.make(scriptType, scriptCode);
            ScriptParseRequestBuilder builder = new ScriptParseRequestBuilder();
            builder.url(record.url).text(record.text).customMap(record.customMap).redirectURL(record.redirectURL);
            return script.doExecute(builder.build());
        } catch (Exception e) {
            ParseResponse response = ParseResponse.buildError(String.format("执行脚本异常:%s", e.getMessage()));
            if (script != null) {
                response.stdout = script.getStdout();
                response.stderr = script.getStderr();
            }
            return response;
        } finally {
            if (script != null) script.close();
        }
    }

    /**
     * 获取匹配规则
     *
     * @param seed 种子
     * @param planId 计划ID
     * @return 存在匹配规则返回，否则返回null
     */
    private Rule getMatchedRule(SeedRecord seed, String planId) {
        if (seed.isScopeExtract()) return null;
        List<Rule> matchedRules = ruleService.getRuleIds(planId).stream().map(id -> {
            Rule rule = ruleService.getCache(id);
            return rule != null && rule.match(seed.url) ? rule : null;
        }).filter(Objects::nonNull).toList();
        if (matchedRules.isEmpty()) throw new NotFoundException("未发现匹配规则");
        if (matchedRules.size() > 1) throw new IllegalStateException("存在多条匹配规则");
        return matchedRules.get(0);
    }
}
