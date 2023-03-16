package xin.manong.darwin.parse.groovy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * groovy脚本cache
 *
 * @author frankcl
 * @date 2023-03-16 15:06:10
 */
public class GroovyScriptCache {

    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptCache.class);

    private Map<String, GroovyScript> groovyScriptMap;

    public GroovyScriptCache() {
        groovyScriptMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加/更新groovy脚本
     * 如果存在脚本，销毁原来脚本
     *
     * @param groovyScript groovy脚本
     */
    public void put(GroovyScript groovyScript) {
        if (groovyScript == null) return;
        GroovyScript previous = groovyScriptMap.put(groovyScript.getKey(), groovyScript);
        if (previous != null) previous.close();
    }

    /**
     * 根据key获取groovy脚本
     *
     * @param key 脚本key
     * @return 如果存在返回groovy脚本，否则返回null
     */
    public GroovyScript get(String key) {
        return groovyScriptMap.get(key);
    }

    /**
     * 判断脚本是否改变
     * 1. 缓存中不存在脚本
     * 2. 缓存中存在脚本，并且脚本MD5改变
     *
     * @param groovyScript 脚本
     * @return 改变返回true，否则返回false
     */
    public boolean isChange(GroovyScript groovyScript) {
        if (groovyScript == null) return false;
        if (!groovyScriptMap.containsKey(groovyScript.getKey())) return true;
        GroovyScript other = groovyScriptMap.get(groovyScript.getKey());
        return other == null || (other.getScriptMD5() != groovyScript.getScriptMD5() &&
                !other.getScriptMD5().equals(groovyScript.getScriptMD5()));
    }
}
