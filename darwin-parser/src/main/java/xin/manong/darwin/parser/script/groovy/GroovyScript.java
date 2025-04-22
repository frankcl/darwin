package xin.manong.darwin.parser.script.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.script.CompileException;
import xin.manong.darwin.parser.script.ConcurrentException;
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

    private static final String METHOD_EXECUTE = "execute";

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

    public GroovyScript(String scriptCode) throws CompileException {
        super(DigestUtils.md5Hex(scriptCode));
        this.classLoader = new GroovyClassLoader();
        buildGroovyObject(scriptCode);
    }

    /**
     * 构建Groovy脚本对象
     *
     * @param scriptCode 脚本代码
     */
    private void buildGroovyObject(String scriptCode) throws CompileException {
        Class<?> groovyClass = null;
        try {
            groovyClass = classLoader.parseClass(scriptCode, key);
            if (!HTMLParser.class.isAssignableFrom(groovyClass)) {
                logger.error("Must inherit from xin.manong.darwin.parser.sdk.HTMLParser");
                throw new CompileException("解析类必须继承xin.manong.darwin.parser.sdk.HTMLParser");
            }
            groovyClass.getMethod(METHOD_EXECUTE, ParseRequest.class);
            this.groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            logger.error("Parse method:{} is not found for parser:{}", METHOD_EXECUTE, groovyClass.getName());
            logger.error(e.getMessage(), e);
            throw new CompileException(String.format("解析类必须实现方法%s", METHOD_EXECUTE), e);
        } catch (Exception e) {
            logger.error("Build groovy script failed for key:{}", key);
            logger.error(e.getMessage(), e);
            throw new CompileException(e.getMessage(), e);
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
        if (groovyObject == null) throw new ConcurrentException();
        return (ParseResponse) groovyObject.invokeMethod(METHOD_EXECUTE, request);
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
