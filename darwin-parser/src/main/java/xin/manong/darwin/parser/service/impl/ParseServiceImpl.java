package xin.manong.darwin.parser.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.script.ScriptCache;
import xin.manong.darwin.parser.script.ScriptCompileException;
import xin.manong.darwin.parser.script.ScriptFactory;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.HTMLScriptRequest;
import xin.manong.darwin.parser.service.response.CompileResponse;

import javax.annotation.Resource;

/**
 * 解析服务实现
 *
 * @author frankcl
 * @date 2023-03-23 15:54:38
 */
@Service
public class ParseServiceImpl implements ParseService {

    private static final Logger logger = LoggerFactory.getLogger(ParseServiceImpl.class);

    @Resource
    protected ScriptCache scriptCache;

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
            ScriptFactory.make(scriptType, scriptCode);
            return CompileResponse.buildOK();
        } catch (ScriptCompileException e) {
            logger.error("{} compile failed", Constants.SUPPORT_SCRIPT_TYPES.get(scriptType));
            logger.error(e.getMessage(), e);
            return CompileResponse.buildError(String.format("脚本编译失败[%s]", e.getMessage()));
        }
    }

    @Override
    public ParseResponse parse(HTMLScriptRequest request) {
        if (request == null || !request.check()) {
            logger.error("parse request is invalid");
            return ParseResponse.buildError("解析请求非法");
        }
        String key = DigestUtils.md5Hex(request.scriptCode);
        Script script = scriptCache.get(key);
        if (script == null) {
            try {
                script = ScriptFactory.make(request.scriptType, request.scriptCode);
                scriptCache.put(script);
            } catch (ScriptCompileException e) {
                logger.error("compile {} failed", Constants.SUPPORT_SCRIPT_TYPES.get(request.scriptType));
                return ParseResponse.buildError(String.format("编译脚本[%s]失败",
                        Constants.SUPPORT_SCRIPT_TYPES.get(request.scriptType)));
            }
        }
        return script.execute(request);
    }
}
