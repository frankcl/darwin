package xin.manong.darwin.parser.service.impl;

import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.script.*;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.LinkExtractService;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.response.CompileResult;

/**
 * 解析服务实现
 *
 * @author frankcl
 * @date 2023-03-23 15:54:38
 */
@Service
public class ParseServiceImpl implements ParseService {

    private static final Logger logger = LoggerFactory.getLogger(ParseServiceImpl.class);

    private static final String COMPILE_HTML = "<html><head></head><body></body></html>";
    private static final String COMPILE_URL = "http://www.test.com/";
    private static final int RETRY_COUNT = 3;

    @Resource
    protected ScriptCache scriptCache;
    @Resource
    protected LinkExtractService linkExtractService;

    @Override
    public CompileResult compile(CompileRequest request) {
        try (Script script = ScriptFactory.make(request.scriptType, request.script)) {
            ParseRequestBuilder builder = new ParseRequestBuilder();
            ParseRequest parseRequest = builder.html(COMPILE_HTML).url(COMPILE_URL).build();
            script.doExecute(parseRequest);
            return CompileResult.success();
        } catch (Exception e) {
            logger.error("Compile failed");
            logger.error(e.getMessage(), e);
            return CompileResult.error(e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public ParseResponse parse(ScriptParseRequest request) {
        if (request == null || !request.check()) {
            logger.error("Parse request is invalid");
            return ParseResponse.buildError("解析请求非法");
        }
        if (request.isScopeExtract()) return linkExtractService.extract(request);
        String key = DigestUtils.md5Hex(request.scriptCode);
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
                script = ScriptFactory.make(request.scriptType, request.scriptCode);
                scriptCache.put(script);
                return script;
            }
        } catch (CompileException e) {
            logger.error("{} script compile failed", Constants.SUPPORT_SCRIPT_TYPES.get(request.scriptType));
            return null;
        }
    }
}
