package xin.manong.darwin.spider;

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
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.parse.service.ParseService;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
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

    private static final String CHARSET_UTF8 = "UTF-8";

    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;
    @Resource
    protected MultiQueue multiQueue;

    public HTMLSpider() {
        super("html");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Rule rule = getMatchRule(record, context);
        if (rule == null) return;
        String html = fetchAndGetHTML(record, context);
        if (html == null) return;
        if (!writeHTML(html, record, context)) return;
        if (!parseHTML(html, record, rule, context)) return;
        record.status = Constants.URL_STATUS_SUCCESS;
    }

    /**
     * 解析HTML文本
     *
     * @param html HTML文本
     * @param record URL记录
     * @param rule 解析规则
     * @param context 上下文
     * @return 成功返回true，否则返回false
     */
    private boolean parseHTML(String html, URLRecord record, Rule rule, Context context) {
        Long startTime = System.currentTimeMillis();
        try {
            ParseRequest request = new ParseRequest.Builder().content(html).record(record).build();
            ParseResponse response = parseService.parse(rule, request);
            if (!response.status) {
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, response.message);
                logger.error("parse HTML failed for url[{}], cause[{}]", record.url, response.message);
                return false;
            }
            if (response.structureMap != null && !response.structureMap.isEmpty()) {
                record.structureMap = response.structureMap;
            }
            if (response.userDefinedMap != null && !response.userDefinedMap.isEmpty()) {
                if (record.userDefinedMap == null) record.userDefinedMap = new HashMap<>();
                record.userDefinedMap.putAll(response.userDefinedMap);
            }
            processLinks(response.followLinks, record, context);
            return true;
        } finally {
            context.put(Constants.DARWIN_PARSE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 将抓取HTML文本写入OSS
     *
     * @param html HTML文本
     * @param record URL记录
     * @param context 上下文
     * @return 成功返回true，否则返回false
     */
    private boolean writeHTML(String html, URLRecord record, Context context) {
        Long startTime = System.currentTimeMillis();
        try {
            byte[] bytes = html.getBytes(Charset.forName(CHARSET_UTF8));
            String suffix = buildResourceFileSuffix(record);
            String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
            if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
            if (!ossClient.putObject(config.contentBucket, key, bytes)) {
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "上传OSS内容失败");
                return false;
            }
            OSSMeta ossMeta = new OSSMeta();
            ossMeta.region = config.contentRegion;
            ossMeta.bucket = config.contentBucket;
            ossMeta.key = key;
            record.fetchContentURL = OSSClient.buildURL(ossMeta);
            return true;
        } finally {
            context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 抓取或获取HTML文本资源
     *
     * @param record URL记录
     * @param context 上下文
     * @return 成功返回HTML文本，否则返回null
     */
    private String fetchAndGetHTML(URLRecord record, Context context) throws Exception {
        Long startTime = System.currentTimeMillis();
        ByteArrayOutputStream outputStream = null;
        SpiderResource resource = getSpiderResource(record, context);
        try {
            if (resource == null) resource = fetchResource(record, context);
            if (resource == null || resource.inputStream == null) return null;
            record.mimeType = resource.mimeType;
            record.subMimeType = resource.subMimeType;
            int size = 4096, n;
            byte[] buffer = new byte[size];
            outputStream = new ByteArrayOutputStream();
            while ((n = resource.inputStream.read(buffer, 0, size)) != -1) outputStream.write(buffer, 0, n);
            byte[] body = outputStream.toByteArray();
            String charset = resource.guessCharset ? parseCharset(body, resource.charset) : CHARSET_UTF8;
            return new String(body, Charset.forName(charset));
        } catch (Exception e) {
            record.status = Constants.URL_STATUS_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "读取资源流异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("read resource input stream failed");
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            try {
                context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
                if (outputStream != null) outputStream.close();
                if (resource != null) resource.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 根据URL记录获取匹配规则
     *
     * @param record URL记录
     * @param context 上下文
     * @return 匹配规则，无匹配返回null
     */
    private Rule getMatchRule(URLRecord record, Context context) {
        Job job = jobService.getCache(record.jobId);
        if (job == null) {
            record.status = Constants.URL_STATUS_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "爬虫任务不存在");
            logger.error("job[{}] is not found for url[{}]", record.jobId, record.url);
            return null;
        }
        List<Rule> rules = new ArrayList<>();
        for (Integer ruleId : job.ruleIds) {
            Rule rule = ruleService.getCache(ruleId.longValue());
            if (rule == null || !ruleService.match(record, rule)) continue;
            rules.add(rule);
        }
        if (rules.size() != 1) {
            record.status = Constants.URL_STATUS_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("匹配规则数量[%d]不符合预期", rules.size()));
            logger.error("match rule num[{}] is unexpected", rules.size());
            return null;
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
     * @param c HTTP响应头字符编码
     * @return 成功返回编码，否则返回默认编码UTF-8
     */
    private String parseCharset(byte[] body, Charset c) {
        if (c != null) return c.name();
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
            Document document = Jsoup.parse(new String(body, Charset.forName("UTF-8")));
            Element head = document.head();
            if (head == null) return null;
            Elements elements = head.select("meta[http-equiv=content-type]");
            for (int i = 0; elements != null && i < elements.size(); i++) {
                Element element = elements.get(i);
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
                }
            }
        } catch (Exception e) {
            logger.error("parse charset failed from HTML");
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 处理抽链结果
     *
     * @param records 抽链数据列表
     * @param parentRecord 父链接
     * @param context 上下文
     */
    private void processLinks(List<URLRecord> records, URLRecord parentRecord, Context context) {
        if (records == null || records.isEmpty()) return;
        int discardLinkNum = 0;
        for (URLRecord record : records) {
            Context linkContext = new Context();
            try {
                record.appId = parentRecord.appId;
                record.jobId = parentRecord.jobId;
                record.parentURL = parentRecord.url;
                record.depth = parentRecord.depth + 1;
                record.status = Constants.URL_STATUS_CREATED;
                if (record.priority == null) record.priority = parentRecord.priority;
                if (record.concurrentLevel == null) record.concurrentLevel = parentRecord.concurrentLevel;
                if (record.fetchMethod == null) record.fetchMethod = parentRecord.fetchMethod;
                if (parentRecord.userDefinedMap != null && !parentRecord.userDefinedMap.isEmpty()) {
                    Map<String, Object> userDefinedMap = record.userDefinedMap;
                    record.userDefinedMap = new HashMap<>();
                    record.userDefinedMap.putAll(parentRecord.userDefinedMap);
                    record.userDefinedMap.putAll(userDefinedMap);
                }
                if (!record.check()) {
                    discardLinkNum++;
                    logger.warn("follow link[{}] is invalid for parent url[{}]", record.url, parentRecord.url);
                    continue;
                }
                multiQueue.push(record, 3);
            } catch (Exception e) {
                linkContext.put(Constants.DARWIN_DEBUG_MESSAGE, "处理抽链异常");
                linkContext.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
                logger.error(e.getMessage(), e);
            } finally {
                DarwinUtil.putContext(linkContext, record);
                linkContext.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_FOLLOW_LINK);
                if (aspectLogger != null) aspectLogger.commit(linkContext.getFeatureMap());
            }
        }
        context.put(Constants.FOLLOW_LINK_NUM, records.size());
        context.put(Constants.DISCARD_FOLLOW_LINK_NUM, discardLinkNum);
    }
}
