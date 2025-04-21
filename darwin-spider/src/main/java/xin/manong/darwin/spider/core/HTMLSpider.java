package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import xin.manong.darwin.queue.PushResult;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.spider.input.ByteArrayInput;
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.output.ByteArrayOutput;
import xin.manong.weapon.base.common.Context;

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

    private static final String CATEGORY = "html";

    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;
    @Resource
    protected HttpClientFactory httpClientFactory;

    public HTMLSpider() {
        super(CATEGORY);
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        boolean scopeExtract = record.isScopeExtract();
        Rule rule = scopeExtract ? null : findMatchRule(record);
        if (!scopeExtract && rule == null) throw new IllegalStateException("no matched rule");
        Input input = buildInput(record, context);
        if (input instanceof HTTPInput) {
            record.html = fetch(record, (HTTPInput) input, context);
            input = new ByteArrayInput(record.html.getBytes(StandardCharsets.UTF_8));
        }
        write(record, input, context);
        parse(record, rule, context);
    }

    /**
     * 抓取URL
     *
     * @param record URL数据
     * @throws Exception 异常
     */
    public void fetch(URLRecord record) throws Exception {
        HTTPInput input = new HTTPInput(record, httpClientFactory.getHttpClient(record), config);
        record.html = fetch(record, input, null);
    }

    /**
     * 抓取URL
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
     * 抓取数据
     *
     * @param record URL数据
     * @param context 上下文
     * @return HTML字符串
     * @throws Exception 异常
     */
    private String fetch(URLRecord record, HTTPInput input, Context context) throws Exception {
        long startTime = System.currentTimeMillis();
        try (input; ByteArrayOutput byteArrayOutput = new ByteArrayOutput()) {
            input.open();
            input.transport(byteArrayOutput);
            byte[] byteArray = byteArrayOutput.getBytes();
            String charset = speculateCharset(byteArray, record);
            if (context != null) context.put(Constants.CHARSET, charset);
            return new String(byteArray, Charset.forName(charset));
        } finally {
            if (context != null) context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 解析HTML文本
     *
     * @param record URL记录
     * @param rule 解析规则
     * @param context 上下文
     */
    private void parse(URLRecord record, Rule rule, Context context) {
        long startTime = System.currentTimeMillis();
        try {
            ScriptParseRequestBuilder builder = new ScriptParseRequestBuilder().html(record.html).
                    url(record.url).redirectURL(record.redirectURL).userDefinedMap(record.userDefinedMap);
            if (record.isScopeExtract()) builder.linkScope(record.scope);
            else builder.scriptType(rule.scriptType).scriptCode(rule.script);
            ScriptParseRequest request = builder.build();
            ParseResponse response = parseService.parse(request);
            if (!response.status) {
                throw new IllegalStateException(String.format("parse HTML failed for url: %s", record.url));
            }
            if (response.fieldMap != null && !response.fieldMap.isEmpty()) record.fieldMap = response.fieldMap;
            if (response.userDefinedMap != null && !response.userDefinedMap.isEmpty()) {
                if (record.userDefinedMap == null) record.userDefinedMap = new HashMap<>();
                record.userDefinedMap.putAll(response.userDefinedMap);
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
     * 推送子链
     *
     * @param children 子链列表
     * @param parent 父链
     * @param context 上下文
     */
    private void push(List<URLRecord> children, URLRecord parent, Context context) {
        if (children == null || children.isEmpty()) {
            context.put(Constants.CHILDREN, 0);
            context.put(Constants.INVALID_CHILDREN, 0);
            return;
        }
        context.put(Constants.CHILDREN, children.size());
        context.put(Constants.INVALID_CHILDREN, children.stream().filter(child -> !push(child, parent)).count());
    }

    /**
     * 推送URL到抓取队列
     *
     * @param child 子链
     * @param parent 父链
     * @return 推动成功返回true，否则返回false
     */
    private boolean push(URLRecord child, URLRecord parent) {
        Context context = new Context();
        try {
            context.put(Constants.DARWIN_STAGE, Constants.STAGE_EXTRACT);
            fillChild(child, parent);
            if (!child.check()) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "子链非法");
                logger.warn("child[{}] is invalid for parent[{}]", child.url, parent.url);
                return false;
            }
            if (!urlService.add(child)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "子链入库失败");
                logger.warn("add child[{}] failed for parent[{}]", child.url, parent.url);
                return false;
            }
            PushResult status = concurrencyQueue.push(child, 3);
            if (status != PushResult.SUCCESS) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("子链入队失败: %s", status.name()));
                logger.warn("push child[{}] failed for parent[{}], queue status[{}]",
                        child.url, parent.url, status.name());
                return false;
            }
            return true;
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "处理子链异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            DarwinUtil.putContext(context, child);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }

    /**
     * 使用父链信息填充子链信息
     *
     * @param child 子链
     * @param parent 父链
     */
    private void fillChild(URLRecord child, URLRecord parent) {
        child.appId = parent.appId;
        child.jobId = parent.jobId;
        child.planId = parent.planId;
        child.parentURL = parent.url;
        child.depth = parent.depth + 1;
        child.status = Constants.URL_STATUS_CREATED;
        if (child.priority == null) child.priority = parent.priority;
        if (child.fetchMethod == null) child.fetchMethod = parent.fetchMethod;
        if (child.concurrentLevel == null && DarwinUtil.isSameHost(child, parent)) {
            child.concurrentLevel = parent.concurrentLevel;
        }
        if (parent.userDefinedMap != null && !parent.userDefinedMap.isEmpty()) {
            Map<String, Object> userDefinedMap = child.userDefinedMap;
            child.userDefinedMap = new HashMap<>();
            child.userDefinedMap.putAll(parent.userDefinedMap);
            child.userDefinedMap.putAll(userDefinedMap);
        }
    }
}
