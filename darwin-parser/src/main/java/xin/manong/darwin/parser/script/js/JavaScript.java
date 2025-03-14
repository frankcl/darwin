package xin.manong.darwin.parser.script.js;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.script.ScriptCompileException;
import xin.manong.darwin.parser.script.ScriptConcurrentException;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaScript脚本
 *
 * @author frankcl
 * @date 2023-03-17 14:39:22
 */
public class JavaScript extends Script {

    private static final Logger logger = LoggerFactory.getLogger(JavaScript.class);

    private static final String METHOD_PARSE = "parse";
    private static final String JAVASCRIPT_UTILS_FILE = "/js/parse_utils.js";
    private static final String JAVASCRIPT_ENGINE_NAME = "nashorn";
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final String JAVASCRIPT_UTILS = loadJavaScriptUtils();

    private Invocable function;

    /**
     * 加载JavaScript工具函数
     * 失败抛出异常
     *
     * @return JavaScript工具函数
     */
    private static String loadJavaScriptUtils() {
        int bufferSize = 4096, n;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = JavaScript.class.getResourceAsStream(JAVASCRIPT_UTILS_FILE);
            if (inputStream == null) throw new IllegalStateException("load javascript utils file failed");
            outputStream = new ByteArrayOutputStream(bufferSize);
            byte[] buffer = new byte[bufferSize];
            while ((n = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, n);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("load java script utils failed, cause[{}]", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public JavaScript() {
        super(null);
    }

    public JavaScript(String scriptCode) throws ScriptCompileException {
        super(DigestUtils.md5Hex(scriptCode));
        buildJavaScriptFunction(scriptCode);
    }

    /**
     * 构建JavaScript调用方法
     *
     * @param scriptCode JavaScript脚本代码
     * @throws ScriptCompileException 编译失败抛出该异常
     */
    private void buildJavaScriptFunction(String scriptCode) throws ScriptCompileException {
        try {
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(JAVASCRIPT_ENGINE_NAME);
            scriptEngine.eval(String.format("%s\n%s", JAVASCRIPT_UTILS, scriptCode));
            function = (Invocable) scriptEngine;
        } catch (Exception e) {
            logger.error("build JavaScript parse script failed for key[{}]", key);
            logger.error(e.getMessage(), e);
            throw new ScriptCompileException(String.format("创建JavaScript脚本失败[%s]", e.getMessage()), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseResponse doExecute(ParseRequest request) throws Exception {
        if (function == null) throw new ScriptConcurrentException();
        ScriptObjectMirror scriptObject = (ScriptObjectMirror) function.invokeFunction(METHOD_PARSE, request);
        Map<String, Object> map = (Map<String, Object>) convertScriptObjectMirror(scriptObject);
        if (map == null) return ParseResponse.buildError("解析响应为空");
        ParseResponse response = JSON.toJavaObject(new JSONObject(map), ParseResponse.class);
        if (response.status && response.childURLs != null) {
            for (URLRecord childURL : response.childURLs) {
                if (childURL.url != null) childURL.hash = DigestUtils.md5Hex(childURL.url);
            }
        }
        return response;
    }

    @Override
    public void doClose() throws IOException {
        if (function != null) function = null;
    }

    /**
     * 转化JavaScript对象，结果为Map或List
     *
     * @param scriptObject JavaScript对象
     * @return Map或List对象
     */
    @SuppressWarnings("unchecked")
    private Object convertScriptObjectMirror(ScriptObjectMirror scriptObject) {
        if (scriptObject == null) return null;
        boolean isArray = scriptObject.isArray();
        Object returnObject = isArray ? new ArrayList<>() : new HashMap<>();
        for (Map.Entry<String, Object> entry : scriptObject.entrySet()) {
            Object object = entry.getValue();
            object = object instanceof ScriptObjectMirror ?
                    convertScriptObjectMirror((ScriptObjectMirror) object) : object;
            if (isArray) ((List<Object>) returnObject).add(object);
            else ((Map<Object, Object>) returnObject).put(entry.getKey(), object);
        }
        return returnObject;
    }
}
