package xin.manong.darwin.spider;

import jakarta.annotation.Resource;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequestBuilder;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueStatus;
import xin.manong.darwin.service.component.CharsetDetector;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网页爬虫
 * 1. HTML网页
 * 2. JSON内容
 * 3. 其他文本内容
 *
 * @author frankcl
 * @date 2023-03-24 16:21:30
 */
@Component
public class HTMLSpider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(HTMLSpider.class);

    private static final int BUFFER_SIZE = 4096;
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String CATEGORY = "html";

    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;
    @Resource
    protected MultiQueue multiQueue;

    public HTMLSpider() {
        super(CATEGORY);
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        boolean scopeExtract = record.isScopeExtract();
        Rule rule = scopeExtract ? null : findMatchRule(record);
        if (!scopeExtract && rule == null) throw new IllegalStateException("未找到匹配规则");
        InputStream inputStream = getPrevInputStream(record, context);
        boolean prev = inputStream != null;
        Response httpResponse = null;
        try {
            if (inputStream == null) {
                httpResponse = httpRequest(record);
                inputStream = getHTTPInputStream(httpResponse, record);
            }
            String html = readHTML(inputStream, record, context, prev);
            writeHTML(html, record, context);
            parseHTML(html, record, rule, context);
        } finally {
            if (inputStream != null) inputStream.close();
            if (httpResponse != null) httpResponse.close();
        }
    }

    /**
     * 从输入流读取HTML
     *
     * @param inputStream 输入流
     * @param record URL数据
     * @param context 上下文
     * @param prev 是否使用以前抓取数据
     * @return HTML字符串
     */
    private String readHTML(InputStream inputStream, URLRecord record, Context context, boolean prev) {
        if (inputStream == null) throw new IllegalStateException("获取数据输入流失败");
        long startTime = System.currentTimeMillis();
        try (inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int n;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((n = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) outputStream.write(buffer, 0, n);
            byte[] body = outputStream.toByteArray();
            String charset = prev ? CHARSET_UTF8 : parseCharset(body, record.charset);
            context.put(Constants.CHARSET, charset);
            return new String(body, Charset.forName(charset));
        } catch (Exception e) {
            record.status = Constants.URL_STATUS_IO_ERROR;
            logger.error("read {} input stream failed", prev ? "OSS" : "HTML");
            throw new IllegalStateException(prev ? "读取OSS数据失败" : "读取HTML数据失败", e);
        } finally {
            context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 将抓取HTML文本写入OSS
     *
     * @param html HTML文本
     * @param record URL记录
     * @param context 上下文
     */
    private void writeHTML(String html, URLRecord record, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            String suffix = generateSuffixUsingMimeType(record);
            String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
            if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
            if (!ossClient.putObject(config.contentBucket, key, bytes)) {
                record.status = Constants.URL_STATUS_IO_ERROR;
                logger.error("write HTML into OSS failed for URL[{}]", record.url);
                throw new IOException("HTML写入OSS失败");
            }
            OSSMeta ossMeta = new OSSMeta();
            ossMeta.region = config.contentRegion;
            ossMeta.bucket = config.contentBucket;
            ossMeta.key = key;
            record.fetchContentURL = OSSClient.buildURL(ossMeta);
        } finally {
            context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 解析HTML文本
     *
     * @param html HTML文本
     * @param record URL记录
     * @param rule 解析规则
     * @param context 上下文
     */
    private void parseHTML(String html, URLRecord record, Rule rule, Context context) {
        long startTime = System.currentTimeMillis();
        try {
            ScriptParseRequestBuilder builder = new ScriptParseRequestBuilder().html(html).
                    url(record.url).redirectURL(record.redirectURL).userDefinedMap(record.userDefinedMap);
            if (record.isScopeExtract()) builder.linkScope(record.scope);
            else builder.scriptType(rule.scriptType).scriptCode(rule.script);
            ScriptParseRequest request = builder.build();
            ParseResponse response = parseService.parse(request);
            if (!response.status) {
                record.status = Constants.URL_STATUS_PARSE_ERROR;
                logger.error("parse HTML failed for URL[{}], cause[{}]", record.url, response.message);
                throw new IllegalStateException("解析HTML失败");
            }
            if (response.fieldMap != null && !response.fieldMap.isEmpty()) {
                record.fieldMap = response.fieldMap;
            }
            if (response.userDefinedMap != null && !response.userDefinedMap.isEmpty()) {
                if (record.userDefinedMap == null) record.userDefinedMap = new HashMap<>();
                record.userDefinedMap.putAll(response.userDefinedMap);
            }
            processChildURLs(response.childURLs, record, context);
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
        List<Integer> ruleIds = ruleService.getPlanRuleIds(record.planId);
        List<Rule> rules = new ArrayList<>();
        for (Integer ruleId : ruleIds) {
            Rule rule = ruleService.getCache(ruleId);
            if (rule == null || !rule.match(record.url)) continue;
            rules.add(rule);
        }
        if (rules.size() != 1) {
            logger.error("match rule num[{}] is unexpected", rules.size());
            throw new IllegalStateException("存在多条匹配规则");
        }
        return rules.get(0);
    }

    /**
     * 解析HTML编码
     * 1. 从HTTP响应头中获取编码
     * 2. 从HTML正文head->meta中获取编码
     * 3. 猜测HTML编码
     *
     * @param body HTML字节数组
     * @param headerCharset HTTP响应头字符编码
     * @return 返回编码
     */
    private String parseCharset(byte[] body, Charset headerCharset) {
        if (headerCharset != null) return headerCharset.name();
        String charset = parseCharsetFromHTML(body);
        return StringUtils.isEmpty(charset) ? CharsetDetector.detect(body) : charset;
    }

    /**
     * 从HTML中解析编码
     *
     * @param body HTML字节数组
     * @return 成功返回编码，否则返回null
     */
    private String parseCharsetFromHTML(byte[] body) {
        try {
            Document document = Jsoup.parse(new String(body, StandardCharsets.UTF_8));
            Element head = document.head();
            Elements elements = head.select("meta[http-equiv=content-type]");
            for (Element element : elements) {
                if (!element.hasAttr("content")) continue;
                String content = element.attr("content");
                if (StringUtils.isEmpty(content)) continue;
                int index = content.indexOf("charset=");
                if (index == -1) continue;
                String charset = content.substring(index + "charset=".length()).trim();
                try {
                    Charset.forName(charset);
                    return charset;
                } catch (Exception e) {
                    logger.warn("invalid charset[{}]", charset);
                }
            }
        } catch (Exception e) {
            logger.error("parse charset failed from HTML");
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 处理抽链URL列表
     *
     * @param childURLs 抽链URL列表
     * @param parentRecord 父链URL
     * @param context 上下文
     */
    private void processChildURLs(List<URLRecord> childURLs, URLRecord parentRecord, Context context) {
        context.put(Constants.CHILD_URL_COUNT, childURLs == null || childURLs.isEmpty() ? 0 : childURLs.size());
        if (childURLs == null || childURLs.isEmpty()) return;
        int failedChildURLCount = 0;
        for (URLRecord childURL : childURLs) {
            if (processChildURL(childURL, parentRecord)) continue;
            failedChildURLCount++;
        }
        context.put(Constants.FAILED_CHILD_URL_COUNT, failedChildURLCount);
    }

    /**
     * 处理抽链URL
     *
     * @param childURL 抽链URL
     * @param parentRecord 父链URL
     * @return 处理成功返回true，否则返回false
     */
    private boolean processChildURL(URLRecord childURL, URLRecord parentRecord) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_EXTRACT);
        try {
            childURL.appId = parentRecord.appId;
            childURL.jobId = parentRecord.jobId;
            childURL.planId = parentRecord.planId;
            childURL.parentURL = parentRecord.url;
            childURL.depth = parentRecord.depth + 1;
            childURL.status = Constants.URL_STATUS_CREATED;
            if (childURL.priority == null) childURL.priority = parentRecord.priority;
            if (childURL.fetchMethod == null) childURL.fetchMethod = parentRecord.fetchMethod;
            if (childURL.concurrentLevel == null && DarwinUtil.isSameHost(childURL, parentRecord)) {
                childURL.concurrentLevel = parentRecord.concurrentLevel;
            }
            if (parentRecord.userDefinedMap != null && !parentRecord.userDefinedMap.isEmpty()) {
                Map<String, Object> userDefinedMap = childURL.userDefinedMap;
                childURL.userDefinedMap = new HashMap<>();
                childURL.userDefinedMap.putAll(parentRecord.userDefinedMap);
                childURL.userDefinedMap.putAll(userDefinedMap);
            }
            if (!childURL.check()) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "非法抽链结果");
                logger.warn("child URL[{}] is invalid for parent URL[{}]", childURL.url, parentRecord.url);
                return false;
            }
            if (!urlService.add(childURL)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "新增抽链结果失败");
                logger.warn("add child URL[{}] failed for parent URL[{}]", childURL.url, parentRecord.url);
                return false;
            }
            MultiQueueStatus status = multiQueue.push(childURL, 3);
            if (status != MultiQueueStatus.OK) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("新链接添加多级队列失败[%s]", status.name()));
                logger.warn("push child URL[{}] into MultiQueue failed, status[{}]", childURL.url, status.name());
                return false;
            }
            return true;
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "处理抽链结果异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            DarwinUtil.putContext(context, childURL);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }
}
