package xin.manong.darwin.parser.script.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.script.ScriptCompileException;
import xin.manong.darwin.parser.script.ScriptConcurrentException;
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

    private static final String METHOD_PARSE = "doParse";

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
                logger.error("parse groovy failed");
                throw new ScriptCompileException("无效Groovy脚本");
            }
            groovyClass.getMethod(METHOD_PARSE, ParseRequest.class);
            this.groovyObject = (GroovyObject) groovyClass.newInstance();
        } catch (NoSuchMethodException e) {
            logger.error("parse method[{}] is not found for parser[{}]", METHOD_PARSE,
                    groovyClass != null ? groovyClass.getName() : "");
            logger.error(e.getMessage(), e);
            throw new ScriptCompileException(String.format("未找到解析方法[%s]", METHOD_PARSE), e);
        } catch (Exception e) {
            logger.error("build Groovy script failed for id[{}]", key);
            logger.error(e.getMessage(), e);
            throw new ScriptCompileException(String.format("构建Groovy脚本失败[%s]", e.getMessage()), e);
        }
    }


    /**
     * 执行解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    @Override
    public ParseResponse doExecute(ParseRequest request) throws Exception {
        if (groovyObject == null) throw new ScriptConcurrentException();
        return (ParseResponse) groovyObject.invokeMethod(METHOD_PARSE, request);
    }

    /**
     * 关闭销毁groovy脚本对象，防止内存溢出
     */
    @Override
    public void doClose() throws IOException {
        groovyObject = null;
        if (classLoader != null) {
            classLoader.close();
            classLoader = null;
        }
    }
}
