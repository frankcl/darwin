package xin.manong.darwin.parse.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.parse.parser.Parser;

import java.io.IOException;

/**
 * groovy脚本封装
 *
 * @author frankcl
 * @date 2023-03-16 14:13:12
 */
public class GroovyScript {

    /**
     * groovy脚本对象构建器
     */
    public static class Builder {
        private GroovyScript template;

        public Builder() {
            template = new GroovyScript();
        }

        public Builder key(String key) {
            template.key = key;
            return this;
        }

        public Builder scriptMD5(String scriptMD5) {
            template.scriptMD5 = scriptMD5;
            return this;
        }

        public Builder classLoader(GroovyClassLoader classLoader) {
            template.classLoader = classLoader;
            return this;
        }

        public Builder scriptObject(GroovyObject scriptObject) {
            template.scriptObject = scriptObject;
            return this;
        }

        public GroovyScript build() {
            GroovyScript groovyScript = new GroovyScript();
            groovyScript.key = template.key;
            groovyScript.scriptMD5 = template.scriptMD5;
            groovyScript.classLoader = template.classLoader;
            groovyScript.scriptObject = template.scriptObject;
            return groovyScript;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(GroovyScript.class);

    private static final String METHOD_PARSE = "parse";

    /**
     * 脚本key
     */
    private String key;
    /**
     * 脚本MD5签名
     */
    private String scriptMD5;
    /**
     * groovy脚本对象类加载器
     */
    private GroovyClassLoader classLoader;
    /**
     * groovy脚本对象
     */
    private GroovyObject scriptObject;

    private GroovyScript() {
    }

    public GroovyScript(String key, String script) {
        this.key = key;
        this.scriptMD5 = DigestUtils.md5Hex(script);
        this.classLoader = new GroovyClassLoader();
        buildScriptObject(script);
    }

    public GroovyScript(String key, String script, ClassLoader classLoader) {
        this.key = key;
        this.scriptMD5 = DigestUtils.md5Hex(script);
        this.classLoader = new GroovyClassLoader(classLoader);
        buildScriptObject(script);
    }

    /**
     * 构建脚本对象
     *
     * @param script 脚本
     */
    private void buildScriptObject(String script) {
        try {
            Class scriptClass = classLoader.parseClass(script, scriptMD5);
            if (!Parser.class.isAssignableFrom(scriptClass)) {
                logger.error("parser[{}] not extends class[{}]", scriptClass.getName(), Parser.class.getName());
                throw new RuntimeException(String.format("parser[%s] not extends class[%s]",
                        scriptClass.getName(), Parser.class.getName()));
            }
            this.scriptObject = (GroovyObject) scriptClass.newInstance();
        } catch (Exception e) {
            logger.error("create groovy script object failed for key[{}]", key);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(String.format("创建groovy脚本对象[%s]失败", key));
        }
    }


    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public ParseResponse execute(ParseRequest request) {
        return (ParseResponse) scriptObject.invokeMethod(METHOD_PARSE, request);
    }

    /**
     * 关闭销毁groovy脚本对象，防止内存溢出
     */
    public void close() {
        scriptObject = null;
        try {
            if (classLoader != null) {
                classLoader.close();
                classLoader = null;
            }
        } catch (IOException e) {
            logger.warn("close class loader failed for groovy script[{}]", key);
        }
    }

    /**
     * 获取key
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取脚本MD5签名
     *
     * @return 脚本MD5签名
     */
    public String getScriptMD5() {
        return scriptMD5;
    }
}
