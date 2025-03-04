package xin.manong.darwin.parser.service.impl;

import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.script.*;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ScopeExtractService;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.HTMLParseRequest;
import xin.manong.darwin.parser.service.response.CompileResponse;

/**
 * 解析服务实现
 *
 * @author frankcl
 * @date 2023-03-23 15:54:38
 */
@Service
public class ParseServiceImpl implements ParseService {

    private static final Logger logger = LoggerFactory.getLogger(ParseServiceImpl.class);

    private static final int RETRY_COUNT = 3;

    @Resource
    protected ScriptCache scriptCache;
    @Resource
    protected ScopeExtractService scopeExtractService;

    @Override
    public CompileResponse compile(int scriptType, String scriptCode) {
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("unsupported script type[{}]", scriptType);
            return CompileResponse.buildError(String.format("不支持的脚本类型[%d]", scriptType));
        }
        if (StringUtils.isEmpty(scriptCode)) {
            logger.error("script code is empty");
            return CompileResponse.buildError("脚本代码为空");
        }
        try {
            Script script = ScriptFactory.make(scriptType, scriptCode);
            script.close();
            return CompileResponse.buildOK();
        } catch (ScriptCompileException e) {
            logger.error("{} compile failed", Constants.SUPPORT_SCRIPT_TYPES.get(scriptType));
            logger.error(e.getMessage(), e);
            return CompileResponse.buildError(String.format("编译脚本失败[%s]", e.getMessage()));
        }
    }

    @Override
    public ParseResponse parse(HTMLParseRequest request) {
        if (request == null || !request.check()) {
            logger.error("parse request is invalid");
            return ParseResponse.buildError("解析请求非法");
        }
        if (request.isScopeExtract()) return scopeExtractService.parse(request);
        String key = DigestUtils.md5Hex(request.scriptCode);
        for (int i = 0; i < RETRY_COUNT; i++) {
            Script script = buildScript(key, request);
            if (script == null) {
                return ParseResponse.buildError(String.format("编译脚本[%s]失败",
                        Constants.SUPPORT_SCRIPT_TYPES.get(request.scriptType)));
            }
            try {
                return script.execute(request);
            } catch (ScriptConcurrentException e) {
                logger.warn("script concurrent exception occurred");
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
    private Script buildScript(String key, HTMLParseRequest request) {
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
        } catch (ScriptCompileException e) {
            logger.error("compile {} failed", Constants.SUPPORT_SCRIPT_TYPES.get(request.scriptType));
            return null;
        }
    }
}
