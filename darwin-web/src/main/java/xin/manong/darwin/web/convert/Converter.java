package xin.manong.darwin.web.convert;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.web.request.*;
import xin.manong.hylian.model.User;

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
        app.comment = request.comment;
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
        app.comment = request.comment;
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
        appUser.nickName = request.nickName;
        return appUser;
    }

    /**
     * 转换批量更新应用用户关系请求
     *
     * @param request 批量更新应用用户关系请求
     * @return 应用用户关系列表
     */
    public static List<AppUser> convert(BatchAppUserRequest request) {
        List<AppUser> appUsers = new ArrayList<>();
        if (request == null || request.users == null) return appUsers;
        for (User user : request.users) {
            AppUser appUser = new AppUser();
            appUser.appId = request.appId;
            appUser.userId = user.id;
            appUser.nickName = user.name;
            appUsers.add(appUser);
        }
        return appUsers;
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
        plan.allowRepeat = request.allowRepeat != null && request.allowRepeat;
        plan.status = false;
        plan.fetchMethod = request.fetchMethod == null ? Constants.FETCH_METHOD_COMMON : request.fetchMethod;
        plan.priority = request.priority == null ? Constants.PRIORITY_NORMAL : request.priority;
        plan.category = request.category;
        plan.crontabExpression = request.crontabExpression;
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
        plan.allowRepeat = request.allowRepeat;
        plan.priority = request.priority;
        plan.category = request.category;
        plan.fetchMethod = request.fetchMethod;
        plan.appId = request.appId;
        plan.appName = request.appName;
        plan.crontabExpression = request.crontabExpression;
        return plan;
    }

    /**
     * 转换种子URL请求为种子URL记录
     *
     * @param request 种子URL请求
     * @return 种子URL记录
     */
    public static SeedRecord convert(SeedRequest request) {
        if (request == null) return null;
        SeedRecord record = new SeedRecord(request.url);
        record.planId = request.planId;
        record.fetchMethod = request.fetchMethod;
        record.scope = request.scope;
        record.category = request.category;
        record.concurrentLevel = request.concurrentLevel;
        record.priority = request.priority;
        record.timeout = request.timeout != null && request.timeout <= 0 ? null : request.timeout;
        record.headers = request.headers == null ? new HashMap<>() : new HashMap<>(request.headers);
        record.userDefinedMap = request.userDefinedMap == null ? new HashMap<>() : new HashMap<>(request.userDefinedMap);
        return record;
    }

    /**
     * 转换种子URL更新请求为种子URL记录
     *
     * @param request 种子URL更新请求
     * @return 种子URL记录
     */
    public static SeedRecord convert(SeedUpdateRequest request) {
        if (request == null) return null;
        SeedRecord record = new SeedRecord(request.url);
        record.key = request.key;
        record.fetchMethod = request.fetchMethod;
        record.scope = request.scope;
        record.category = request.category;
        record.concurrentLevel = request.concurrentLevel;
        record.priority = request.priority;
        record.timeout = request.timeout;
        record.headers = request.headers == null ? new HashMap<>() : new HashMap<>(request.headers);
        record.userDefinedMap = request.userDefinedMap == null ? new HashMap<>() : new HashMap<>(request.userDefinedMap);
        record.createTime = null;
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
        rule.name = request.name;
        rule.regex = request.regex;
        rule.script = request.script;
        rule.scriptType = request.scriptType;
        rule.planId = request.planId;
        return rule;
    }

    /**
     * 转换规则请求更新为规则对象
     *
     * @param request 规则请求
     * @return 规则对象
     */
    public static Rule convert(RuleUpdateRequest request) {
        if (request == null) return null;
        Rule rule = new Rule();
        rule.id = request.id;
        rule.name = request.name;
        rule.regex = request.regex;
        rule.script = request.script;
        rule.scriptType = request.scriptType;
        return rule;
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
        Proxy proxy = convert((ProxyRequest) request);
        proxy.id = request.id;
        return proxy;
    }
}
