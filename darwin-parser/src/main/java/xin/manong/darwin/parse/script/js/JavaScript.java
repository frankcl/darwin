package xin.manong.darwin.parse.script.js;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.parse.script.Script;
import xin.manong.weapon.base.util.RandomID;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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

    /**
     * JavaScript脚本对象构建器
     */
    public static class Builder {
        private JavaScript template;

        public Builder() {
            template = new JavaScript();
        }

        public Builder id(Long id) {
            template.id = id;
            return this;
        }

        public Builder scriptMD5(String scriptMD5) {
            template.scriptMD5 = scriptMD5;
            return this;
        }

        public Builder function(Invocable function) {
            template.function = function;
            return this;
        }

        public JavaScript build() {
            JavaScript javaScript = new JavaScript();
            javaScript.id = template.id;
            javaScript.scriptMD5 = template.scriptMD5;
            javaScript.function = template.function;
            return javaScript;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(JavaScript.class);

    private static final String COMMON_JAVASCRIPT_RESOURCE = "/js/parse_utils.js";
    private static final String JAVASCRIPT_ENGINE_NAME = "JavaScript";
    private static final String METHOD_PARSE = "parse";
    private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final String COMMON_JAVASCRIPT = loadCommonJavaScript();

    private Invocable function;

    private static String loadCommonJavaScript() {
        int bufferSize = 4096, n;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = JavaScript.class.getResourceAsStream(COMMON_JAVASCRIPT_RESOURCE);
            outputStream = new ByteArrayOutputStream(bufferSize);
            byte[] buffer = new byte[bufferSize];
            while ((n = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, n);
            return new String(outputStream.toByteArray(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            logger.error("load common java script failed, cause[{}]", e.getMessage());
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

    private JavaScript() {
    }

    public JavaScript(Long id, String script) {
        super(id, DigestUtils.md5Hex(script));
        buildInvocable(script);
    }

    /**
     * 构建JavaScript调用方法
     *
     * @param script
     */
    private void buildInvocable(String script) {
        try {
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(JAVASCRIPT_ENGINE_NAME);
            scriptEngine.eval(String.format("%s\n%s", COMMON_JAVASCRIPT, script));
            function = (Invocable) scriptEngine;
        } catch (Exception e) {
            logger.error("build JavaScript function failed for id[{}]", id);
            throw new RuntimeException(String.format("构建JavaScript脚本方法[%d]失败", id));
        }
    }

    @Override
    public ParseResponse execute(ParseRequest request) {
        try {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) function.invokeFunction(METHOD_PARSE, request);
            Map<String, Object> map = (Map<String, Object>) convertScriptObjectToMapList(scriptObjectMirror);
            if (map == null) return ParseResponse.buildErrorResponse("解析响应为空");
            ParseResponse response = JSON.toJavaObject(new JSONObject(map), ParseResponse.class);
            if (response.status && response.followLinks != null) {
                for (URLRecord followLink : response.followLinks) {
                    if (followLink.url != null) followLink.hash = DigestUtils.md5Hex(followLink.url);
                }
            }
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ParseResponse.buildErrorResponse(String.format("执行脚本异常[%s]", e.getMessage()));
        }
    }

    @Override
    public void close() {
        if (function != null) function = null;
    }

    /**
     * 转化ScriptObjectMirror为Map或List
     *
     * @param scriptObjectMirror
     * @return Map或List
     */
    private Object convertScriptObjectToMapList(ScriptObjectMirror scriptObjectMirror) {
        if (scriptObjectMirror == null) return null;
        if (scriptObjectMirror.isArray()) {
            List<Object> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
                Object object = entry.getValue();
                if (object instanceof ScriptObjectMirror) {
                    list.add(convertScriptObjectToMapList((ScriptObjectMirror) object));
                } else {
                    list.add(object);
                }
            }
            return list;
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
            Object object = entry.getValue();
            if (object instanceof ScriptObjectMirror) {
                map.put(entry.getKey(), convertScriptObjectToMapList((ScriptObjectMirror) object));
            } else {
                map.put(entry.getKey(), object);
            }
        }
        return map;
    }
}
