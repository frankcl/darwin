package xin.manong.darwin.parser.script;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.script.groovy.GroovyScript;
import xin.manong.darwin.parser.script.js.JavaScript;

/**
 * 脚本构建工厂
 *
 * @author frankcl
 * @date 2023-03-27 19:55:16
 */
public class ScriptFactory {

    private static final Logger logger = LoggerFactory.getLogger(ScriptFactory.class);

    /**
     * 根据脚本类型及脚本代码构建脚本执行对象
     *
     * @param scriptType 脚本类型
     * @param scriptCode 脚本代码
     * @throws CompileException 编译失败抛出该异常
     * @return 脚本执行对象
     */
    public static Script make(int scriptType, String scriptCode) throws CompileException {
        if (StringUtils.isEmpty(scriptCode)) {
            logger.error("Script is empty");
            throw new CompileException("脚本代码为空");
        }
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("Unsupported script type:{}", scriptType);
            throw new CompileException("脚本类型不支持");
        }
        if (scriptType == Constants.SCRIPT_TYPE_GROOVY) return new GroovyScript(scriptCode);
        else if (scriptType == Constants.SCRIPT_TYPE_JAVASCRIPT) return new JavaScript(scriptCode);
        throw new CompileException("脚本类型不支持");
    }
}
