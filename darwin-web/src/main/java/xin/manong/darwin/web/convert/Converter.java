package xin.manong.darwin.web.convert;

import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.web.request.*;

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
        plan.seedURLs = request.seedURLs;
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
        plan.seedURLs = request.seedURLs;
        return plan;
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
        rule.category = request.category;
        rule.linkScope = request.linkScope;
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
}
