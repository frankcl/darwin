package xin.manong.darwin.parser.script.js;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.parser.script.Script;
import xin.manong.darwin.parser.script.ConcurrentException;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JavaScript脚本
 *
 * @author frankcl
 * @date 2023-03-17 14:39:22
 */
public class JavaScript extends Script {

    private static final Logger logger = LoggerFactory.getLogger(JavaScript.class);

    private static final String LANGUAGE = "js";
    private static final String METHOD_PARSE = "parse";
    private static final String DARWIN_FILE = "/js/darwin.js";
    private static final String OPTION_COMMON_JS_REQUIRE = "js.commonjs-require";
    private static final String OPTION_COMMON_JS_REQUIRE_CWD = "js.commonjs-require-cwd";

    private Context context;
    private final String requireCwd;
    private ByteArrayOutputStream stdout;
    private ByteArrayOutputStream stderr;

    public JavaScript() {
        super(null);
        requireCwd = null;
    }

    public JavaScript(String scriptCode, String requireCwd) throws IOException {
        super(DigestUtils.md5Hex(String.format("%d_%s", Thread.currentThread().getId(), scriptCode)));
        this.requireCwd = requireCwd;
        buildContext(scriptCode);
    }

    /**
     * 关闭输出
     *
     * @throws IOException I/O异常
     */
    private void closeOutput() throws IOException {
        if (stdout != null) {
            stdout.close();
            stdout = null;
        }
        if (stderr != null) {
            stderr.close();
            stderr = null;
        }
    }

    /**
     * 读取JavaScript文件
     *
     * @param file 脚本文件
     * @return 脚本内容
     * @throws IOException I/O异常
     */
    private String readJavaScriptFile(String file) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             InputStream input = JavaScript.class.getResourceAsStream(file)) {
            assert input != null;
            input.transferTo(output);
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * 构建上下文对象
     *
     * @param scriptCode 脚本代码
     * @throws IOException 异常
     */
    private void buildContext(String scriptCode) throws IOException {
        try {
            stdout = new ByteArrayOutputStream();
            stderr = new ByteArrayOutputStream();
            context = Context.newBuilder(LANGUAGE).
                    allowHostAccess(HostAccess.ALL).
                    allowHostClassLookup(className -> className.equals(JavaScript.class.getName())).
                    allowExperimentalOptions(true).
                    allowIO(IOAccess.newBuilder().allowHostFileAccess(true).build()).
                    option(OPTION_COMMON_JS_REQUIRE, "true").
                    option(OPTION_COMMON_JS_REQUIRE_CWD, requireCwd).
                    out(stdout).err(stderr).
                    build();
            context.eval(Source.create(LANGUAGE, readJavaScriptFile(DARWIN_FILE)));
            context.eval(LANGUAGE, scriptCode);
        } catch (Exception e) {
            closeOutput();
            logger.error("Build JavaScript context failed for key:{}", key);
            logger.error(e.getMessage(), e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseResponse doExecute(ParseRequest request) throws Exception {
        if (context == null) throw new ConcurrentException();
        Value parseFunction = context.getBindings(LANGUAGE).getMember(METHOD_PARSE);
        if (parseFunction == null) {
            logger.error("function:{} is not found", METHOD_PARSE);
            throw new IOException(String.format("未定义方法:%s", METHOD_PARSE));
        }
        if (stdout != null) stdout.reset();
        if (stderr != null) stderr.reset();
        Map<String, Object> map = parseFunction.execute(request).as(Map.class);
        if (map == null) return ParseResponse.buildError("解析响应为空");
        ParseResponse response = JSON.parseObject(JSON.toJSONString(map), ParseResponse.class);
        if (stdout != null) response.stdout = stdout.toString(StandardCharsets.UTF_8);
        if (stderr != null) response.stderr = stderr.toString(StandardCharsets.UTF_8);
        if (response.status && response.children != null) {
            for (URLRecord child : response.children) {
                if (child.url != null) child.setUrl(child.url);
            }
        }
        return response;
    }

    @Override
    public String getStdout() {
        if (stdout != null) return stdout.toString(StandardCharsets.UTF_8);
        return null;
    }

    @Override
    public String getStderr() {
        if (stderr != null) return stderr.toString(StandardCharsets.UTF_8);
        return null;
    }

    @Override
    public void doClose() throws IOException {
        closeOutput();
        if (context != null) context.close();
    }

    /**
     * 正规化HTML
     *
     * @param html HTML
     * @return 正规化HTML
     */
    public static String normalizeHTML(String html) {
        if (html == null) return null;
        Document.OutputSettings outputSettings = new Document.OutputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .charset(StandardCharsets.UTF_8)
                .prettyPrint(true);
        return Jsoup.parse(html).outputSettings(outputSettings).outerHtml();
    }
}
