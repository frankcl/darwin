package xin.manong.darwin.parse.script;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.parse.script.groovy.GroovyScript;

/**
 * @author frankcl
 * @date 2023-03-16 17:51:07
 */
public class ScriptCacheSuite {

    @Test
    public void testCache() {
        ScriptCache cache = new ScriptCache();
        {
            GroovyScript groovyScript = new GroovyScript.Builder().key("123").scriptMD5("aaa").build();
            Assert.assertTrue(cache.isChange(groovyScript));
            cache.put(groovyScript);
            Assert.assertFalse(cache.isChange(groovyScript));
        }
        {
            GroovyScript groovyScript = new GroovyScript.Builder().key("123").scriptMD5("bbb").build();
            Assert.assertTrue(cache.isChange(groovyScript));
            cache.put(groovyScript);
        }
        Script groovyScript = cache.get("123");
        Assert.assertTrue(groovyScript != null);
        Assert.assertEquals("123", groovyScript.getKey());
        Assert.assertEquals("bbb", groovyScript.getScriptMD5());
    }
}
