package xin.manong.darwin.parse.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parse.script.groovy.GroovyScript;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * groovy脚本cache
 *
 * @author frankcl
 * @date 2023-03-16 15:06:10
 */
public class ScriptCache {

    private static final Logger logger = LoggerFactory.getLogger(ScriptCache.class);

    private Map<String, Script> scriptMap;

    public ScriptCache() {
        scriptMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加/更新脚本
     * 如果存在脚本，销毁原来脚本
     *
     * @param script 脚本
     */
    public void put(Script script) {
        if (script == null) return;
        Script previous = scriptMap.put(script.getKey(), script);
        if (previous != null) previous.close();
    }

    /**
     * 根据key获取脚本
     *
     * @param key 脚本key
     * @return 如果存在返回脚本，否则返回null
     */
    public Script get(String key) {
        return scriptMap.get(key);
    }

    /**
     * 判断脚本是否改变
     * 1. 缓存中不存在脚本
     * 2. 缓存中存在脚本，并且脚本MD5改变
     *
     * @param script 脚本
     * @return 改变返回true，否则返回false
     */
    public boolean isChange(Script script) {
        if (script == null) return false;
        if (!scriptMap.containsKey(script.getKey())) return true;
        Script other = scriptMap.get(script.getKey());
        return other == null || (other.getScriptMD5() != script.getScriptMD5() &&
                !other.getScriptMD5().equals(script.getScriptMD5()));
    }
}
