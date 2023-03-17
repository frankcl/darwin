package xin.manong.darwin.parse.script;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.parse.script.groovy.GroovyScript;
import xin.manong.darwin.parse.script.js.JavaScript;

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
        {
            JavaScript javaScript = new JavaScript.Builder().key("456").scriptMD5("bbb").build();
            Assert.assertTrue(cache.isChange(javaScript));
            cache.put(javaScript);
        }
        Script groovyScript = cache.get("123");
        Assert.assertTrue(groovyScript != null);
        Assert.assertEquals("123", groovyScript.getKey());
        Assert.assertEquals("bbb", groovyScript.getScriptMD5());

        Script javaScript = cache.get("456");
        Assert.assertTrue(javaScript != null);
        Assert.assertEquals("456", javaScript.getKey());
        Assert.assertEquals("bbb", javaScript.getScriptMD5());
    }
}
