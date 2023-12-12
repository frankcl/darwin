package xin.manong.darwin.web.convert;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.web.request.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 数据转换
 *
 * @author frankcl
 * @date 2023-10-20 14:00:19
 */
public class Converter {

    /**
     * 转换应用请求为应用对象
     *
     * @param request 应用请求
     * @return 应用对象
     */
    public static App convert(AppRequest request) {
        if (request == null) return null;
        App app = new App();
        app.name = request.name;
        return app;
    }

    /**
     * 转换更新应用请求为应用对象
     *
     * @param request 更新应用请求
     * @return 应用对象
     */
    public static App convert(AppUpdateRequest request) {
        if (request == null) return null;
        App app = new App();
        app.id = request.id;
        app.name = request.name;
        return app;
    }

    /**
     * 转换应用用户关系请求为应用用户关系对象
     *
     * @param request 应用用户关系请求
     * @return 应用用户关系对象
     */
    public static AppUser convert(AppUserRequest request) {
        if (request == null) return null;
        AppUser appUser = new AppUser();
        appUser.appId = request.appId;
        appUser.userId = request.userId;
        appUser.userRealName = request.realName;
        return appUser;
    }

    /**
     * 转换计划请求为计划对象
     *
     * @param request 计划请求
     * @return 计划对象
     */
    public static Plan convert(PlanRequest request) {
        if (request == null) return null;
        Plan plan = new Plan();
        plan.appId = request.appId;
        plan.appName = request.appName;
        plan.name = request.name;
        plan.avoidRepeatedFetch = request.avoidRepeatedFetch == null ? true : request.avoidRepeatedFetch;
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.priority = request.priority == null ? Constants.PRIORITY_NORMAL : request.priority;
        plan.category = request.category;
        plan.crontabExpression = request.crontabExpression;
        plan.ruleIds = request.ruleIds;
        plan.seedURLs = convert(request.seedURLs);
        return plan;
    }

    /**
     * 转换计划更新请求为计划对象
     *
     * @param request 计划更新请求
     * @return 计划对象
     */
    public static Plan convert(PlanUpdateRequest request) {
        if (request == null) return null;
        Plan plan = new Plan();
        plan.planId = request.planId;
        plan.name = request.name;
        plan.avoidRepeatedFetch = request.avoidRepeatedFetch;
        plan.priority = request.priority;
        plan.category = request.category;
        plan.crontabExpression = request.crontabExpression;
        plan.ruleIds = request.ruleIds;
        plan.seedURLs = convert(request.seedURLs);
        return plan;
    }

    /**
     * 转换URL请求列表为URL记录列表
     *
     * @param requests URL请求列表
     * @return URL记录列表
     */
    public static List<URLRecord> convert(List<URLRequest> requests) {
        List<URLRecord> records = new ArrayList<>();
        if (requests == null || requests.isEmpty()) return records;
        for (URLRequest request : requests) {
            URLRecord record = convert(request);
            if (record == null) continue;
            records.add(record);
        }
        return records;
    }

    /**
     * 转换URL请求为URL记录
     *
     * @param request URL请求
     * @return URL记录
     */
    public static URLRecord convert(URLRequest request) {
        if (request == null) return null;
        URLRecord record = new URLRecord(request.url);
        record.fetchMethod = request.fetchMethod;
        record.scope = request.scope;
        record.category = request.category;
        record.concurrentLevel = request.concurrentLevel;
        record.priority = request.priority;
        record.timeout = request.timeout;
        record.headers = request.headers == null ? new HashMap<>() : new HashMap<>(request.headers);
        record.userDefinedMap = request.userDefinedMap == null ? new HashMap<>() : new HashMap<>(request.userDefinedMap);
        return record;
    }

    /**
     * 转换规则请求为规则对象
     *
     * @param request 规则请求
     * @return 规则对象
     */
    public static Rule convert(RuleRequest request) {
        if (request == null) return null;
        Rule rule = new Rule();
        rule.ruleGroup = request.ruleGroup;
        rule.name = request.name;
        rule.domain = request.domain;
        rule.regex = request.regex;
        rule.script = request.script;
        rule.scriptType = request.scriptType;
        return rule;
    }

    /**
     * 转换规则分组请求为规则分组对象
     *
     * @param request 规则分组请求
     * @return 规则分组对象
     */
    public static RuleGroup convert(RuleGroupRequest request) {
        if (request == null) return null;
        RuleGroup ruleGroup = new RuleGroup();
        ruleGroup.name = request.name;
        return ruleGroup;
    }

    /**
     * 转换规则分组更新请求为规则分组对象
     *
     * @param request 规则分组更新请求
     * @return 规则分组对象
     */
    public static RuleGroup convert(RuleGroupUpdateRequest request) {
        if (request == null) return null;
        RuleGroup ruleGroup = new RuleGroup();
        ruleGroup.id = request.id;
        ruleGroup.name = request.name;
        return ruleGroup;
    }

    /**
     * 转化代理添加请求为代理对象
     *
     * @param request 代理添加请求
     * @return 代理对象
     */
    public static Proxy convert(ProxyRequest request) {
        if (request == null) return null;
        Proxy proxy = new Proxy();
        proxy.address = request.address;
        proxy.port = request.port;
        proxy.category = request.category;
        proxy.username = request.username;
        proxy.password = request.password;
        proxy.expiredTime = request.expiredTime;
        return proxy;
    }

    /**
     * 转化代理更新请求为代理对象
     *
     * @param request 代理更新请求
     * @return 代理对象
     */
    public static Proxy convert(ProxyUpdateRequest request) {
        if (request == null) return null;
        Proxy proxy = new Proxy();
        proxy.id = request.id;
        proxy.address = request.address;
        proxy.port = request.port;
        proxy.category = request.category;
        proxy.username = request.username;
        proxy.password = request.password;
        proxy.expiredTime = request.expiredTime;
        return proxy;
    }
}
