package xin.manong.darwin.parse.script;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.parse.script.groovy.GroovyScript;
import xin.manong.darwin.parse.script.js.JavaScript;

/**
 * 脚本构建工厂
 *
 * @author frankcl
 * @date 2023-03-27 19:55:16
 */
public class ScriptFactory {

    private static final Logger logger = LoggerFactory.getLogger(ScriptFactory.class);

    /**
     * 根据脚本类型及脚本代码构建脚本
     *
     * @param scriptType
     * @param scriptText
     * @return
     */
    public static Script make(int scriptType, String scriptText) {
        if (StringUtils.isEmpty(scriptText)) {
            logger.error("script text is empty");
            throw new RuntimeException("脚本代码为空");
        }
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("unsupported script type[{}]", scriptType);
            throw new RuntimeException(String.format("不支持的脚本类型[%d]", scriptType));
        }
        if (scriptType == Constants.SCRIPT_TYPE_GROOVY) return new GroovyScript(0L, scriptText);
        else if (scriptType == Constants.SCRIPT_TYPE_JAVASCRIPT) return new JavaScript(0L, scriptText);
        throw new RuntimeException(String.format("不支持的脚本类型[%d]", scriptType));
    }

    /**
     * 根据规则构建脚本
     *
     * @param rule 规则
     * @return 脚本
     */
    public static Script make(Rule rule) {
        if (rule == null) {
            logger.error("rule is null");
            throw new RuntimeException("规则为空");
        }
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(rule.scriptType)) {
            logger.error("unsupported script type[{}]", rule.scriptType);
            throw new RuntimeException(String.format("不支持的脚本类型[%d]", rule.scriptType));
        }
        if (rule.scriptType == Constants.SCRIPT_TYPE_GROOVY) return new GroovyScript(rule.id, rule.script);
        else if (rule.scriptType == Constants.SCRIPT_TYPE_JAVASCRIPT) return new JavaScript(rule.id, rule.script);
        throw new RuntimeException(String.format("不支持脚本类型[%d]", rule.scriptType));
    }
}
