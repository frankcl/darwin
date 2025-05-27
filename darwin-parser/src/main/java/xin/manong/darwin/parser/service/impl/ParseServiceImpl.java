package xin.manong.darwin.parser.service.impl;

import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.parser.script.*;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.LinkExtractService;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.request.RuleParseRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequestBuilder;
import xin.manong.darwin.parser.service.response.CompileResult;
import xin.manong.darwin.service.iface.RuleService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 解析服务实现
 *
 * @author frankcl
 * @date 2023-03-23 15:54:38
 */
@Service
public class ParseServiceImpl implements ParseService {

    private static final Logger logger = LoggerFactory.getLogger(ParseServiceImpl.class);

    private static final String COMPILE_HTML = "{}";
    private static final String COMPILE_URL = "http://www.test.com/";
    private static final String GROOVY_TEMPLATE = "/template/groovy.tpl";
    private static final String JS_TEMPLATE = "/template/javascript.tpl";
    private static final int RETRY_COUNT = 3;

    @Resource
    private ScriptCache scriptCache;
    @Resource
    private ScriptFactory scriptFactory;
    @Resource
    private RuleService ruleService;
    @Resource
    private LinkExtractService linkExtractService;

    @Override
    public CompileResult compile(@NonNull CompileRequest request) {
        Script script = null;
        try {
            script = scriptFactory.make(request.scriptType, request.script);
            ParseRequestBuilder builder = new ParseRequestBuilder();
            if (request.scriptType == Constants.SCRIPT_TYPE_GROOVY) {
                ParseRequest parseRequest = builder.text(COMPILE_HTML).url(COMPILE_URL).build();
                script.doExecute(parseRequest);
            }
            return CompileResult.success();
        } catch (Exception e) {
            logger.error("Compile failed");
            logger.error(e.getMessage(), e);
            CompileResult compileResult = CompileResult.error(e.getMessage(), ExceptionUtils.getStackTrace(e));
            if (script != null) {
                compileResult.stdout = script.getStdout();
                compileResult.stderr = script.getStderr();
            }
            return compileResult;
        } finally {
            if (script != null) script.close();
        }
    }

    @Override
    public String scriptTemplate(int scriptType) throws IOException {
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType))
            throw new UnsupportedOperationException("不支持的代码类型");
        try (InputStream input = getClass().getResourceAsStream(
                scriptType == Constants.SCRIPT_TYPE_JAVASCRIPT ? JS_TEMPLATE : GROOVY_TEMPLATE);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IOException("Can't find script template");
            input.transferTo(output);
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    @Override
    public ParseResponse parse(@NonNull ScriptParseRequest request) {
        if (request.isScopeExtract()) return linkExtractService.extract(request);
        String key = DigestUtils.md5Hex(request.scriptCode);
        if (request.scriptType == Constants.SCRIPT_TYPE_JAVASCRIPT) {
            key = DigestUtils.md5Hex(String.format("%d_%s", Thread.currentThread().getId(), request.scriptCode));
        }
        for (int i = 0; i < RETRY_COUNT; i++) {
            Script script = buildScript(key, request);
            if (script == null) {
                return ParseResponse.buildError(String.format("编译脚本[%s]失败",
                        Constants.SUPPORT_SCRIPT_TYPES.get(request.scriptType)));
            }
            try {
                return script.execute(request);
            } catch (ConcurrentException e) {
                logger.warn("Script concurrent exception occurred");
            } finally {
                if (script.currentReferenceCount() <= 0 && scriptCache.get(key) == null) script.close();
            }
        }
        return ParseResponse.buildError("解析失败");
    }

    @Override
    public ParseResponse parse(@NonNull RuleParseRequest request) {
        Rule rule = null;
        if (!request.isScopeExtract()) {
            rule = ruleService.getCache(request.ruleId);
            if (rule == null) throw new NotFoundException("规则不存在");
        }
        ScriptParseRequestBuilder builder = new ScriptParseRequestBuilder();
        if (rule != null) builder.scriptType(rule.scriptType).scriptCode(rule.script);
        ScriptParseRequest scriptParseRequest = builder.text(request.text).url(request.url).
                redirectURL(request.redirectURL).linkScope(request.linkScope).customMap(request.customMap).build();
        return parse(scriptParseRequest);
    }

    /**
     * 编译构建脚本
     * 1. 优先从缓存获取编译脚本
     * 2. 缓存不存在脚本，编译构建脚本并放入缓存
     *
     * @param key 脚本key
     * @param request HTML脚本请求
     * @return 成功返回脚本对象，否则返回null
     */
    private Script buildScript(String key, ScriptParseRequest request) {
        Script script = scriptCache.get(key);
        if (script != null) return script;
        try {
            synchronized (this) {
                script = scriptCache.get(key);
                if (script != null) return script;
                script = scriptFactory.make(request.scriptType, request.scriptCode);
                scriptCache.put(script);
                return script;
            }
        } catch (IOException e) {
            logger.error("Script compile failed");
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
