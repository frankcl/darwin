package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.URLNormalizer;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.RuleParseRequest;
import xin.manong.darwin.parser.service.request.RuleParseRequestBuilder;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.queue.PushResult;
import xin.manong.darwin.service.component.ConcurrencyComputer;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.common.Context;

import java.util.*;

/**
 * 文本解析器
 *
 * @author frankcl
 * @date 2025-05-01 09:52:39
 */
@Component
public class TextParser {

    private static final Logger logger = LoggerFactory.getLogger(TextParser.class);

    @Resource
    private SpiderConfig spiderConfig;
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

    /**
     * 结构化解析数据
     *
     * @param record 数据
     * @param context 上下文
     */
    public void parse(URLRecord record, Context context) {
        Rule rule = findMatchRule(record);
        if (rule == null && !record.isScopeExtract()) return;
        long startTime = System.currentTimeMillis();
        try {
            RuleParseRequestBuilder builder = new RuleParseRequestBuilder().text(record.text).
                    url(record.url).redirectURL(record.redirectURL).customMap(record.customMap);
            if (record.isScopeExtract()) builder.linkScope(record.linkScope);
            if (rule != null) builder.ruleId(rule.id);
            RuleParseRequest request = builder.build();
            ParseResponse response = parseService.parse(request);
            if (!response.status) {
                logger.error("Parse failed for url:{}, cause:{}", record.url, response.stderr);
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
        List<Rule> matchedRules = ruleService.getRules(record.planId).stream().
                filter(rule -> rule.match(record.url)).toList();
        if (matchedRules.isEmpty()) return null;
        if (matchedRules.size() != 1) {
            logger.error("Matched rule num:{} is unexpected", matchedRules.size());
            throw new IllegalStateException("存在多条匹配规则");
        }
        return matchedRules.get(0);
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
        Integer maxDepth = (Integer) context.get(Constants.MAX_DEPTH);
        context.put(Constants.CHILDREN, children.size());
        context.put(Constants.INVALID_CHILDREN, children.stream().filter(
                child -> !push(child, parent, maxDepth)).count());
    }

    /**
     * 推送子链接到抓取队列
     *
     * @param child 子链接
     * @param parent 父链接
     * @param planMaxDepth 计划最大抓取深度
     * @return 推动成功返回true，否则返回false
     */
    private boolean push(URLRecord child, URLRecord parent,
                         Integer planMaxDepth) {
        Context context = new Context();
        try {
            context.put(Constants.DARWIN_STAGE, Constants.PROCESS_STAGE_EXTRACT);
            fillChild(child, parent);
            if (!child.check()) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "链接非法");
                logger.warn("Child:{} is invalid", child.url);
                return false;
            }
            int maxDepth = planMaxDepth == null ? spiderConfig.maxDepth : planMaxDepth;
            if (child.depth >= maxDepth) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "超过最大抽链深度");
                logger.warn("Depth exceeds max depth for child:{}", child.url);
                return false;
            }
            if (child.requestHash.equals(parent.requestHash)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "父链接相同");
                logger.warn("Ignore child:{} same with parent", child.url);
                return false;
            }
            if (urlService.isDuplicate(child)) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "重复子链接");
                logger.warn("Ignore duplicated child:{}", child.url);
                return false;
            }
            if ((child.allowRepeat == null || !child.allowRepeat) && urlService.isFetched(child)) {
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
        child.parentKey = parent.key;
        child.depth = parent.depth + 1;
        child.status = Constants.URL_STATUS_UNKNOWN;
        if (child.mustNormalize()) {
            String normalizedURL = URLNormalizer.normalize(child.url);
            child.setUrl(normalizedURL);
        }
        child.requestHash = child.computeRequestHash();
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
