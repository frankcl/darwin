package xin.manong.darwin.parse.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.parse.script.Script;
import xin.manong.darwin.parse.script.ScriptCache;
import xin.manong.darwin.parse.script.ScriptFactory;
import xin.manong.darwin.service.iface.RuleService;

import javax.annotation.Resource;

/**
 * 解析服务
 *
 * @author frankcl
 * @date 2023-03-23 15:54:38
 */
@Service
public class ParseService {

    private static final Logger logger = LoggerFactory.getLogger(ParseService.class);

    @Resource
    protected RuleService ruleService;
    private ScriptCache scriptCache;

    public ParseService() {
        scriptCache = new ScriptCache();
    }

    /**
     * 根据规则进行解析
     *
     * @param ruleId 规则ID
     * @param request 解析请求
     * @return 解析响应
     */
    public ParseResponse parse(Long ruleId, ParseRequest request) {
        if (ruleId == null) {
            logger.error("rule id is null");
            return ParseResponse.buildErrorResponse("规则ID为空");
        }
        if (request == null || !request.check()) {
            logger.error("parse request is invalid");
            return ParseResponse.buildErrorResponse("解析请求非法");
        }
        Script script = scriptCache.get(ruleId);
        if (script == null) {
            Rule rule = ruleService.getCache(ruleId);
            if (rule == null) {
                logger.error("rule[{}] is not found", ruleId);
                return ParseResponse.buildErrorResponse(String.format("规则[%d]不存在", ruleId));
            }
            script = compileRuleScript(rule);
            if (script == null) return ParseResponse.buildErrorResponse(String.format("编译规则[%d]脚本失败", rule.id));
            scriptCache.put(script);
        }
        return script.execute(request);
    }

    /**
     * 根据规则进行解析
     *
     * @param rule 规则
     * @param request 解析请求
     * @return 解析响应
     */
    public ParseResponse parse(Rule rule, ParseRequest request) {
        if (rule == null) {
            logger.error("rule is null");
            return ParseResponse.buildErrorResponse("规则为空");
        }
        if (request == null || !request.check()) {
            logger.error("parse request is invalid");
            return ParseResponse.buildErrorResponse("解析请求非法");
        }
        Script script = scriptCache.get(rule.id);
        if (script == null) {
            script = compileRuleScript(rule);
            if (script == null) return ParseResponse.buildErrorResponse(String.format("编译规则[%d]脚本失败", rule.id));
            scriptCache.put(script);
        }
        return script.execute(request);
    }

    /**
     * 利用脚本进行解析
     *
     * @param scriptType 脚本类型
     * @param scriptText 脚本文本
     * @param request 解析请求
     * @return 解析响应
     */
    public ParseResponse parse(int scriptType, String scriptText, ParseRequest request) {
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("unsupported script type[{}]", scriptType);
            return ParseResponse.buildErrorResponse(String.format("不支持脚本类型[%d]", scriptType));
        }
        if (StringUtils.isEmpty(scriptText)) {
            logger.error("script is empty");
            return ParseResponse.buildErrorResponse("解析脚本为空");
        }
        if (request == null || !request.check()) {
            logger.error("parse request is invalid");
            return ParseResponse.buildErrorResponse("解析请求非法");
        }
        Script script = null;
        try {
            script = ScriptFactory.make(scriptType, scriptText);
            return script.execute(request);
        } catch (Exception e) {
            logger.error("compile script failed");
            logger.error(e.getMessage(), e);
            return ParseResponse.buildErrorResponse("编译脚本失败");
        } finally {
            if (script != null) script.close();
        }
    }

    /**
     * 编译规则脚本
     *
     * @param rule 规则
     * @return 规则脚本，失败返回null
     */
    private Script compileRuleScript(Rule rule) {
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(rule.scriptType)) {
            logger.error("unsupported script type[{}]", rule.scriptType);
            return null;
        }
        try {
            return ScriptFactory.make(rule);
        } catch (Exception e) {
            logger.error("compile script failed for rule[{}]", rule.id);
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
