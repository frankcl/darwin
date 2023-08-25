package xin.manong.darwin.parser.script.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.script.ScriptCompileException;
import xin.manong.darwin.parser.sdk.HTMLParser;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

import java.io.IOException;

/**
 * groovy脚本封装
 *
 * @author frankcl
 * @date 2023-03-16 14:13:12
 */
public class GroovyScript extends Script {

    private static final Logger logger = LoggerFactory.getLogger(GroovyScript.class);

    private static final String METHOD_PARSE = "parse";

    /**
     * groovy脚本对象类加载器
     */
    private GroovyClassLoader classLoader;
    /**
     * groovy脚本对象
     */
    private GroovyObject groovyObject;

    public GroovyScript() {
        super(null);
    }

    public GroovyScript(String scriptCode) throws ScriptCompileException {
        super(DigestUtils.md5Hex(scriptCode));
        this.classLoader = new GroovyClassLoader();
        buildGroovyObject(scriptCode);
    }

    /**
     * 构建Groovy脚本对象
     *
     * @param scriptCode 脚本代码
     */
    private void buildGroovyObject(String scriptCode) throws ScriptCompileException {
        Class groovyClass = null;
        try {
            groovyClass = classLoader.parseClass(scriptCode, key);
            if (!HTMLParser.class.isAssignableFrom(groovyClass)) {
                logger.error("parser[{}] is not subclass of class[{}]",
                        groovyClass.getName(), HTMLParser.class.getName());
                throw new ScriptCompileException(String.format("解析器[%s]不是%s的子类",
                        groovyClass.getName(), HTMLParser.class.getName()));
            }
            groovyClass.getMethod(METHOD_PARSE, ParseRequest.class);
            this.groovyObject = (GroovyObject) groovyClass.newInstance();
        } catch (NoSuchMethodException e) {
            logger.error("parse method[{}] is not found for parser[{}]", METHOD_PARSE,
                    groovyClass != null ? groovyClass.getName() : "");
            logger.error(e.getMessage(), e);
            throw new ScriptCompileException(String.format("未找到解析方法[%s]", METHOD_PARSE));
        } catch (Exception e) {
            logger.error("create groovy script object failed for id[{}]", key);
            logger.error(e.getMessage(), e);
            throw new ScriptCompileException(String.format("创建groovy脚本对象[%s]失败", key));
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
        try {
            if (groovyObject == null) return ParseResponse.buildError("执行脚本对象未初始化");
            return (ParseResponse) groovyObject.invokeMethod(METHOD_PARSE, request);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ParseResponse.buildError(String.format("执行脚本异常[%s]", e.getMessage()));
        }
    }

    /**
     * 关闭销毁groovy脚本对象，防止内存溢出
     */
    @Override
    public void close() {
        groovyObject = null;
        try {
            if (classLoader != null) {
                classLoader.close();
                classLoader = null;
            }
        } catch (IOException e) {
            logger.warn("close class loader failed for groovy script[{}]", key);
            logger.warn(e.getMessage(), e);
        }
    }
}
