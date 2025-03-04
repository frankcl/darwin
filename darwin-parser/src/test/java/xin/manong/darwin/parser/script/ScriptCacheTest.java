package xin.manong.darwin.parser.script;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.parser.script.groovy.GroovyScript;
import xin.manong.darwin.parser.script.js.JavaScript;

/**
 * @author frankcl
 * @date 2023-03-16 17:51:07
 */
public class ScriptCacheTest {

    @Test
    public void testCache() {
        ScriptCache cache = new ScriptCache(1000, 10);
        {
            GroovyScript groovyScript = new GroovyScript();
            groovyScript.setKey("aaa");
            Assert.assertFalse(cache.contains(groovyScript));
            cache.put(groovyScript);
            Assert.assertTrue(cache.contains(groovyScript));
        }
        {
            JavaScript javaScript = new JavaScript();
            javaScript.setKey("bbb");
            Assert.assertFalse(cache.contains(javaScript));
            cache.put(javaScript);
            Assert.assertTrue(cache.contains(javaScript));
        }
        Script groovyScript = cache.get("aaa");
        Assert.assertEquals("aaa", groovyScript.getKey());
        Assert.assertTrue(groovyScript instanceof GroovyScript);

        Script javaScript = cache.get("bbb");
        Assert.assertNotNull(javaScript);
        Assert.assertEquals("bbb", javaScript.getKey());
        Assert.assertTrue(javaScript instanceof JavaScript);
    }
}
