package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.URLNormalizer;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequestBuilder;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.queue.PushResult;
import xin.manong.darwin.service.component.ConcurrencyComputer;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.spider.input.ByteArrayInput;
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.OSSInput;
import xin.manong.darwin.spider.output.ByteArrayOutput;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本数据爬虫
 *
 * @author frankcl
 * @date 2025-04-27 20:00:39
 */
@Component
public class TextSpider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(TextSpider.class);

    private static final String M3U8_MARK_START = "#EXTM3U";
    private static final String M3U8_MARK_END = "#EXT-X-ENDLIST";

    @Resource
    private HttpClientFactory httpClientFactory;
    @Resource
    private ConcurrencyComputer concurrencyComputer;
    @Resource
    private ConcurrencyQueue concurrencyQueue;
    @Resource
    private ParseService parseService;
    @Resource
    private RuleService ruleService;
    @Resource
    private URLService urlService;
    @Resource
    private AspectLogSupport aspectLogSupport;

    @Override
    public MediaType handle(URLRecord record, Input input, Context context) throws IOException {
        assert input != null;
        record.category = Constants.CONTENT_CATEGORY_PAGE;
        if (input instanceof HTTPInput) {
            record.text = fetch(record, (HTTPInput) input, context);
            if (checkM3U8(record)) return MediaType.STREAM_M3U8;
        } else {
            record.text = read(record, (OSSInput) input, context);
        }
        try (Input byteArrayInput = new ByteArrayInput(record.text.getBytes(StandardCharsets.UTF_8))){
            writer.write(record, byteArrayInput, context);
            parse(record, context);
        }
        return MediaType.UNKNOWN;
    }

    @Override
    public List<MediaType> supportedMediaTypes() {
        return List.of(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XHTML,
                MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.TEXT_CSS, MediaType.TEXT_CSV,
                MediaType.TEXT_JAVASCRIPT, MediaType.APPLICATION_JAVASCRIPT, MediaType.APPLICATION_XML);
    }

    /**
     * 抓取URL
     *
     * @param record URL数据
     * @throws Exception 异常
     */
    public void fetch(URLRecord record) throws Exception {
        HTTPInput input = new HTTPInput(record, httpClientFactory.getHttpClient(record), spiderConfig);
        record.text = fetch(record, input, null);
    }

    /**
     * 抓取URL
     *
     * @param fetchURL 抓取URL
     * @return URL数据
     * @throws Exception 异常
     */
    public URLRecord fetch(String fetchURL) throws Exception {
        URLRecord record = new URLRecord(fetchURL);
        fetch(record);
        return record;
    }

    /**
     * 检测是否为M3U8资源
     *
     * @param record 数据
     * @return 满足条件返回true，否则返回false
     */
    private boolean checkM3U8(URLRecord record) {
        if (!record.mediaType.equals(MediaType.TEXT_PLAIN) || record.text == null) return false;
        String[] textLines = record.text.split("\n");
        return textLines[0].equals(M3U8_MARK_START) && textLines[textLines.length - 1].equals(M3U8_MARK_END);
    }

    /**
     * 从OSS读取数据
     *
     * @param record 数据
     * @param input OSS输入源
     * @param context 上下文
     * @return 文本字符串
     * @throws IOException I/O异常
     */
    private String read(URLRecord record, OSSInput input, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try (ByteArrayOutput byteArrayOutput = new ByteArrayOutput()) {
            if (!input.isOpened()) input.open();
            input.transport(byteArrayOutput);
            byte[] byteArray = byteArrayOutput.getBytes();
            if (record.contentLength == null || record.contentLength == -1) {
                record.contentLength = (long) byteArray.length;
            }
            return new String(byteArray, record.charset);
        } finally {
            if (context != null) context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 抓取数据
     *
     * @param record 数据
     * @param input HTTP数据输入源
     * @param context 上下文
     * @return 文本字符串
     * @throws IOException 异常
     */
    private String fetch(URLRecord record, HTTPInput input, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try (ByteArrayOutput byteArrayOutput = new ByteArrayOutput()) {
            if (!input.isOpened()) input.open();
            if (record.contentLength > spiderConfig.maxContentLength) throw new IllegalStateException("内容长度超过最大限制");
            input.transport(byteArrayOutput);
            byte[] byteArray = byteArrayOutput.getBytes();
            if (record.contentLength == -1) record.contentLength = (long) byteArray.length;
            record.charset = speculateCharset(byteArray, record);
            return new String(byteArray, Charset.forName(record.charset));
        } finally {
            if (context != null) context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推测字符集，顺序如下
     * 1. 使用HTTP response header中设定字符集
     * 2. 使用HTML header中设定字符集
     * 3. 猜测HTML字符集
     * 4. 以上都失败，使用UTF-8字符集
     *
     * @param byteArray HTML字节数组
     * @param record URL数据
     * @return 字符集
     */
    private String speculateCharset(byte[] byteArray, URLRecord record) {
        if (record.mediaType != null && StringUtils.isNotEmpty(
                record.mediaType.charset)) {
            return record.mediaType.charset;
        }
        String charset = HTMLCharsetParser.parse(byteArray);
        if (StringUtils.isNotEmpty(charset)) record.htmlCharset = charset;
        if (StringUtils.isEmpty(charset)) charset = CharsetSpeculator.speculate(byteArray, 0, 1024);
        if (StringUtils.isNotEmpty(charset)) return charset;
        logger.warn("Speculate charset failed, using UTF-8 charset for url: {}", record.url);
        return StandardCharsets.UTF_8.name();
    }

    /**
     * 解析文本
     *
     * @param record 数据
     * @param context 上下文
     */
    private void parse(URLRecord record, Context context) {
        Rule rule = findMatchRule(record);
        if (rule == null && !record.isScopeExtract()) return;
        long startTime = System.currentTimeMillis();
        try {
            ScriptParseRequestBuilder builder = new ScriptParseRequestBuilder().text(record.text).
                    url(record.url).redirectURL(record.redirectURL).customMap(record.customMap);
            if (record.isScopeExtract()) builder.linkScope(record.linkScope);
            else if (rule != null) builder.scriptType(rule.scriptType).scriptCode(rule.script);
            ScriptParseRequest request = builder.build();
            ParseResponse response = parseService.parse(request);
            if (!response.status) {
                logger.error("Parse failed for url: {}", record.url);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("解析失败：%s", response.message));
                return;
            }
            if (response.fieldMap != null && !response.fieldMap.isEmpty()) record.fieldMap = response.fieldMap;
            if (response.customMap != null && !response.customMap.isEmpty()) {
                if (record.customMap == null) record.customMap = new HashMap<>();
                record.customMap.putAll(response.customMap);
            }
            push(response.children, record, context);
        } finally {
            context.put(Constants.DARWIN_PARSE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 根据URL记录获取匹配规则
     *
     * @param record URL记录
     * @return 匹配规则
     */
    private Rule findMatchRule(URLRecord record) {
        if (record.isScopeExtract()) return null;
        List<Integer> ruleIds = ruleService.getRuleIds(record.planId);
        List<Rule> rules = new ArrayList<>();
        for (Integer ruleId : ruleIds) {
            Rule rule = ruleService.getCache(ruleId);
            if (rule == null || !rule.match(record.url)) continue;
            rules.add(rule);
        }
        if (rules.isEmpty()) return null;
        if (rules.size() != 1) {
            logger.error("Match rule num:{} is unexpected", rules.size());
            throw new IllegalStateException("存在多条匹配规则");
        }
        return rules.get(0);
    }

    /**
     * 推送子链接到抓取队列
     *
     * @param children 子链接列表
     * @param parent 父链接
     * @param context 上下文
     */
    private void push(List<URLRecord> children, URLRecord parent, Context context) {
        if (children == null || children.isEmpty()) {
            context.put(Constants.CHILDREN, 0);
            context.put(Constants.INVALID_CHILDREN, 0);
            return;
        }
        boolean allowRepeat = (boolean) context.get(Constants.ALLOW_REPEAT);
        context.put(Constants.CHILDREN, children.size());
        context.put(Constants.INVALID_CHILDREN, children.stream().filter(
                child -> !push(child, parent, allowRepeat)).count());
    }

    /**
     * 推送子链接到抓取队列
     *
     * @param child 子链接
     * @param parent 父链接
     * @param allowRepeat 允许重复抓取
     * @return 推动成功返回true，否则返回false
     */
    private boolean push(URLRecord child, URLRecord parent, boolean allowRepeat) {
        Context context = new Context();
        try {
            context.put(Constants.DARWIN_STAGE, Constants.PROCESS_STAGE_EXTRACT);
            fillChild(child, parent);
            if (!child.check()) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "链接非法");
                logger.warn("Child:{} is invalid", child.url);
                return false;
            }
            if (child.depth >= spiderConfig.maxDepth) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "超过最大抽链深度");
                logger.warn("Depth exceeds max depth for child:{}", child.url);
                return false;
            }
            if (child.url.equals(parent.url)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "父链接相同");
                logger.warn("Ignore child:{} same with parent", child.url);
                return false;
            }
            if (urlService.isDuplicate(child.url, child.jobId)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "重复子链接");
                logger.warn("Ignore duplicated child:{}", child.url);
                return false;
            }
            if (((child.allowRepeat == null && !allowRepeat) ||
                    (child.allowRepeat != null && !child.allowRepeat)) &&
                    urlService.isFetched(child.url, child.planId)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "忽略已抓取链接");
                logger.warn("Ignore fetched child:{}", child.url);
                return false;
            }
            PushResult pushResult = concurrencyQueue.push(child, 3);
            if (pushResult != PushResult.SUCCESS) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送并发队列失败");
                logger.warn("Push queue failed for child:{}, push result is {}", child.url, pushResult.name());
                return false;
            }
            if (!urlService.add(new URLRecord(child))) {
                concurrencyQueue.remove(child);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "添加数据库失败");
                logger.warn("Add child:{} failed", child.url);
                return false;
            }
            return true;
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "处理异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            aspectLogSupport.commitAspectLog(context, child);
        }
    }

    /**
     * 使用父链接信息填充子链接信息
     *
     * @param child 子链接
     * @param parent 父链接
     */
    private void fillChild(URLRecord child, URLRecord parent) {
        child.appId = parent.appId;
        child.jobId = parent.jobId;
        child.planId = parent.planId;
        child.parentURL = parent.url;
        child.depth = parent.depth + 1;
        child.status = Constants.URL_STATUS_UNKNOWN;
        if (child.mustNormalize()) {
            String normalizedURL = URLNormalizer.normalize(child.url);
            child.setUrl(normalizedURL);
        }
        concurrencyComputer.compute(child);
        if (child.priority == null) child.priority = parent.priority;
        if (child.fetchMethod == null) child.fetchMethod = parent.fetchMethod;
        if (parent.customMap != null && !parent.customMap.isEmpty()) {
            Map<String, Object> customMap = child.customMap;
            child.customMap = new HashMap<>();
            child.customMap.putAll(parent.customMap);
            child.customMap.putAll(customMap);
        }
    }
}
