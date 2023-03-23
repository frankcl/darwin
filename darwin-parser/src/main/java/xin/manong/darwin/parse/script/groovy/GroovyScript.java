package xin.manong.darwin.parse.script.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.parse.parser.Parser;
import xin.manong.darwin.parse.script.Script;

import java.io.IOException;

/**
 * groovy脚本封装
 *
 * @author frankcl
 * @date 2023-03-16 14:13:12
 */
public class GroovyScript extends Script {

    /**
     * groovy脚本对象构建器
     */
    public static class Builder {
        private GroovyScript template;

        public Builder() {
            template = new GroovyScript();
        }

        public Builder id(Long id) {
            template.id = id;
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
            groovyScript.id = template.id;
            groovyScript.scriptMD5 = template.scriptMD5;
            groovyScript.classLoader = template.classLoader;
            groovyScript.scriptObject = template.scriptObject;
            return groovyScript;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(GroovyScript.class);

    private static final String METHOD_PARSE = "parse";

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

    public GroovyScript(Long id, String script) {
        super(id, DigestUtils.md5Hex(script));
        this.classLoader = new GroovyClassLoader();
        buildScriptObject(script);
    }

    public GroovyScript(Long id, String script, ClassLoader classLoader) {
        super(id, DigestUtils.md5Hex(script));
        this.classLoader = new GroovyClassLoader(classLoader);
        buildScriptObject(script);
    }

    /**
     * 构建脚本对象
     *
     * @param script 脚本
     */
    private void buildScriptObject(String script) {
        Class scriptClass = null;
        try {
            scriptClass = classLoader.parseClass(script, scriptMD5);
            if (!Parser.class.isAssignableFrom(scriptClass)) {
                logger.error("parser[{}] is not subclass of class[{}]", scriptClass.getName(), Parser.class.getName());
                throw new RuntimeException(String.format("解析器[%s]不是%s的子类",
                        scriptClass.getName(), Parser.class.getName()));
            }
            scriptClass.getMethod(METHOD_PARSE, ParseRequest.class);
            this.scriptObject = (GroovyObject) scriptClass.newInstance();
        } catch (NoSuchMethodException e) {
            logger.error("public method[{}] is not found for parser[{}]", METHOD_PARSE,
                    scriptClass != null ? scriptClass.getName() : "");
            logger.error(e.getMessage(), e);
            throw new RuntimeException(String.format("解析脚本中未找到方法[%s]", METHOD_PARSE));
        } catch (Exception e) {
            logger.error("create groovy script object failed for id[{}]", id);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(String.format("创建groovy脚本对象[%d]失败", id));
        }
    }


    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    @Override
    public ParseResponse execute(ParseRequest request) {
        return (ParseResponse) scriptObject.invokeMethod(METHOD_PARSE, request);
    }

    /**
     * 关闭销毁groovy脚本对象，防止内存溢出
     */
    @Override
    public void close() {
        scriptObject = null;
        try {
            if (classLoader != null) {
                classLoader.close();
                classLoader = null;
            }
        } catch (IOException e) {
            logger.warn("close class loader failed for groovy script[{}]", id);
            logger.warn(e.getMessage(), e);
        }
    }
}
