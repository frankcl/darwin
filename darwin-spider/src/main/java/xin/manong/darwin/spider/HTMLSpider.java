package xin.manong.darwin.spider;

import okhttp3.MediaType;
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
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.parse.service.ParseService;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;

    public HTMLSpider() {
        super("html");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        Long fetchTime = 0L, putTime = 0L, parseTime = 0L;
        try {
            Rule rule = getMatchRule(record, context);
            if (rule == null) {
                record.status = Constants.URL_STATUS_FAIL;
                return;
            }
            Long startFetchTime = System.currentTimeMillis();
            String content = getContentHTML(record, context);
            fetchTime = System.currentTimeMillis() - startFetchTime;
            if (content == null) return;
            byte[] bytes = content.getBytes(Charset.forName("UTF-8"));
            Long startPutTime = System.currentTimeMillis();
            if (!writeContent(record, bytes, context)) {
                putTime = System.currentTimeMillis() - startPutTime;
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取内容写入OSS失败");
                logger.error("write fetch content failed for url[{}]", record.url);
                return;
            }
            putTime = System.currentTimeMillis() - startPutTime;
            Long startParseTime = System.currentTimeMillis();
            ParseRequest request = new ParseRequest.Builder().content(content).record(record).build();
            ParseResponse response = parseService.parse(rule, request);
            parseTime = System.currentTimeMillis() - startParseTime;
            if (!response.status) {
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, response.message);
                logger.error("parse content failed for url[{}], cause[{}]", record.url, response.message);
                return;
            }
            if (response.structureMap != null && !response.structureMap.isEmpty()) {
                record.structureMap = response.structureMap;
            }
            if (response.userDefinedMap != null && !response.userDefinedMap.isEmpty()) {
                if (record.userDefinedMap == null) record.userDefinedMap = new HashMap<>();
                record.userDefinedMap.putAll(response.userDefinedMap);
            }
            processLinks(response.followLinks, record, context);
            record.status = Constants.URL_STATUS_SUCCESS;
        } finally {
            context.put(Constants.DARWIN_FETCH_TIME, fetchTime);
            context.put(Constants.DARWIN_PUT_TIME, putTime);
            context.put(Constants.DARWIN_PARSE_TIME, parseTime);
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
            context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("匹配规则数量[%d]不符合预期", rules.size()));
            logger.error("match rule num[{}] is unexpected", rules.size());
            return null;
        }
        return rules.get(0);
    }

    /**
     * 获取内容HTML
     * 1. 如果避免重复抓取，读取OSS内容
     * 2. 如果不满足1条件，直接抓取
     *
     * @param record URL数据
     * @param context 上下文
     * @return 成功返回HTML，否则返回null
     * @throws IOException
     */
    private String getContentHTML(URLRecord record, Context context) throws IOException {
        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = (InputStream) context.get(Constants.DARWIN_INPUT_STREAM);
        if (inputStream != null) {
            record.fetchTime = System.currentTimeMillis();
            int size = 4096, n;
            byte[] buffer = new byte[size];
            try {
                outputStream = new ByteArrayOutputStream();
                while ((n = inputStream.read(buffer, 0, size)) != -1) {
                    outputStream.write(buffer, 0, n);
                }
                return new String(outputStream.toByteArray(), Charset.forName("UTF-8"));
            } finally {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            }
        }
        Response httpResponse = fetch(record, context);
        if (httpResponse == null) return null;
        String suffix = getResourceSuffix(httpResponse);
        if (!StringUtils.isEmpty(suffix)) context.put(Constants.RESOURCE_SUFFIX, suffix);
        try {
            byte[] body = httpResponse.body().bytes();
            String charset = parseCharset(body, httpResponse);
            return new String(body, charset);
        } finally {
            httpResponse.close();
        }
    }

    /**
     * 解析HTML编码
     * 1. 从HTTP响应头中获取编码
     * 2. 从HTML正文head->meta中获取编码
     * 3. 猜测HTML编码
     *
     * @param body HTML字节数组
     * @param httpResponse HTTP响应
     * @return 成功返回编码，否则返回默认编码UTF-8
     */
    private String parseCharset(byte[] body, Response httpResponse) {
        MediaType mediaType = httpResponse.body().contentType();
        Charset c = mediaType.charset();
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
     * @param followLinks 抽链数据列表
     * @param record 父链接URL
     * @param context 上下文
     */
    private void processLinks(List<URLRecord> followLinks, URLRecord record,
                              Context context) {
        if (followLinks == null || followLinks.isEmpty()) return;
        int discardLinkNum = 0;
        for (URLRecord followLink : followLinks) {
            Context linkContext = new Context();
            try {
                followLink.appId = record.appId;
                followLink.jobId = record.jobId;
                followLink.parentURL = record.url;
                followLink.depth = record.depth + 1;
                followLink.status = Constants.URL_STATUS_CREATED;
                if (followLink.priority == null) followLink.priority = record.priority;
                if (followLink.concurrentLevel == null) followLink.concurrentLevel = record.concurrentLevel;
                if (followLink.fetchMethod == null) followLink.fetchMethod = record.fetchMethod;
                if (record.userDefinedMap != null && !record.userDefinedMap.isEmpty()) {
                    Map<String, Object> userDefinedMap = followLink.userDefinedMap;
                    followLink.userDefinedMap = new HashMap<>();
                    followLink.userDefinedMap.putAll(record.userDefinedMap);
                    followLink.userDefinedMap.putAll(userDefinedMap);
                }
                if (!followLink.check()) {
                    discardLinkNum++;
                    logger.warn("follow link[{}] is invalid for parent url[{}]", followLink.url, record.url);
                    continue;
                }
                multiQueue.push(followLink, 3);
            } catch (Exception e) {
                linkContext.put(Constants.DARWIN_DEBUG_MESSAGE, "处理抽链异常");
                linkContext.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
                logger.error(e.getMessage(), e);
            } finally {
                DarwinUtil.putContext(linkContext, followLink);
                linkContext.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_FOLLOW_LINK);
                if (aspectLogger != null) aspectLogger.commit(linkContext.getFeatureMap());
            }
        }
        context.put(Constants.FOLLOW_LINK_NUM, followLinks.size());
        context.put(Constants.DISCARD_FOLLOW_LINK_NUM, discardLinkNum);
    }
}
