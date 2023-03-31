package xin.manong.darwin.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-03-06 14:27:33
 */
public class Constants {

    /**
     * 支持MimeType
     */
    public static final Set<String> SUPPORT_MIME_TYPES = new HashSet<String>() {{
        add("text");
        add("image");
        add("video");
        add("application");
    }};

    /**
     * URL状态
     */
    public static final int URL_STATUS_SUCCESS = 0;             //抓取成功
    public static final int URL_STATUS_FAIL = -1;               //抓取失败
    public static final int URL_STATUS_CREATED = 1;             //创建
    public static final int URL_STATUS_QUEUING_REFUSED = 2;     //排队拒绝
    public static final int URL_STATUS_QUEUING = 3;             //排队中
    public static final int URL_STATUS_FETCHING = 4;            //抓取中
    public static final int URL_STATUS_INVALID = 5;             //URL非法
    public static final Map<Integer, String> SUPPORT_URL_STATUSES = new HashMap<Integer, String>() {{
        put(URL_STATUS_SUCCESS, "成功");
        put(URL_STATUS_FAIL, "失败");
        put(URL_STATUS_CREATED, "创建");
        put(URL_STATUS_QUEUING_REFUSED, "排队拒绝");
        put(URL_STATUS_QUEUING, "排队中");
        put(URL_STATUS_FETCHING, "抓取中");
        put(URL_STATUS_INVALID, "非法状态");
    }};

    /**
     * 抓取方式
     */
    public static final int FETCH_METHOD_COMMON = 0;            //普通抓取
    public static final int FETCH_METHOD_PROXY = 1;             //代理抓取
    public static final int FETCH_METHOD_HEADLESS_RENDER = 2;   //无头渲染
    public static final int FETCH_METHOD_HEAD_RENDER = 3;       //有头渲染
    public static final Map<Integer, String> SUPPORT_FETCH_METHODS = new HashMap<Integer, String>() {{
       put(FETCH_METHOD_COMMON, "普通抓取");
       put(FETCH_METHOD_PROXY, "代理抓取");
       put(FETCH_METHOD_HEADLESS_RENDER, "无头渲染");
//       put(FETCH_METHOD_HEAD_RENDER, "有头渲染");
    }};

    /**
     * 数据爬取优先级
     */
    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_LOW = 2;

    /**
     * 抓取并发级别
     */
    public static final int CONCURRENT_LEVEL_DOMAIN = 0;
    public static final int CONCURRENT_LEVEL_HOST = 1;
    public static final Map<Integer, String> SUPPORT_CONCURRENT_LEVELS = new HashMap<Integer, String>() {{
       put(CONCURRENT_LEVEL_DOMAIN, "DOMAIN");
       put(CONCURRENT_LEVEL_HOST, "HOST");
    }};

    /**
     * 内容分类
     */
    public static final int CONTENT_CATEGORY_TEXT = 0;              //内容文本
    public static final int CONTENT_CATEGORY_LIST = 1;              //内容列表页
    public static final int CONTENT_CATEGORY_RESOURCE = 2;          //资源：图片视频等
    public static final int CONTENT_CATEGORY_STREAM = 3;            //流：视频流等
    public static final Map<Integer, String> SUPPORT_CONTENT_CATEGORIES = new HashMap<Integer, String>() {{
        put(CONTENT_CATEGORY_TEXT, "文本内容");
        put(CONTENT_CATEGORY_LIST, "文本列表");
        put(CONTENT_CATEGORY_RESOURCE, "资源");
        put(CONTENT_CATEGORY_STREAM, "流媒体");
    }};

    /**
     * 脚本类型
     */
    public static final int SCRIPT_TYPE_GROOVY = 1;                 //Groovy脚本
    public static final int SCRIPT_TYPE_JAVASCRIPT = 2;             //JavaScript脚本
    public static final Map<Integer, String> SUPPORT_SCRIPT_TYPES = new HashMap<Integer, String>() {{
        put(SCRIPT_TYPE_GROOVY, "Groovy");
        put(SCRIPT_TYPE_JAVASCRIPT, "JavaScript");
    }};

    /**
     * 规则分类
     */
    public static final int RULE_CATEGORY_LINK_FOLLOW = 1;           //抽链规则
    public static final int RULE_CATEGORY_STRUCTURE = 2;             //结构化规则
    public static final int RULE_CATEGORY_GLOBAL_LINK_FOLLOW = 3;    //全局抽链规则
    public static final Map<Integer, String> SUPPORT_RULE_CATEGORIES = new HashMap<Integer, String>() {{
        put(RULE_CATEGORY_LINK_FOLLOW, "抽链规则");
        put(RULE_CATEGORY_GLOBAL_LINK_FOLLOW, "通用抽链规则");
        put(RULE_CATEGORY_STRUCTURE, "结构化规则");
    }};

    /**
     * 抽链范围
     */
    public static final int LINK_FOLLOW_SCOPE_ALL = 0;          //全局抽链
    public static final int LINK_FOLLOW_SCOPE_DOMAIN = 1;       //domain抽链
    public static final int LINK_FOLLOW_SCOPE_HOST = 2;         //host抽链
    public static final Map<Integer, String> SUPPORT_LINK_FOLLOW_SCOPES = new HashMap<Integer, String>() {{
        put(LINK_FOLLOW_SCOPE_ALL, "ALL");
        put(LINK_FOLLOW_SCOPE_DOMAIN, "DOMAIN");
        put(LINK_FOLLOW_SCOPE_HOST, "HOST");
    }};

    /**
     * 计划分类
     */
    public static final int PLAN_CATEGORY_ONCE = 0;         //一次性计划任务
    public static final int PLAN_CATEGORY_REPEAT = 1;       //周期性计划任务
    public static final int PLAN_CATEGORY_CONSUME = 2;      //消费型计划任务
    public static final Map<Integer, String> SUPPORT_PLAN_CATEGORIES = new HashMap<Integer, String>() {{
        put(PLAN_CATEGORY_ONCE, "一次性计划");
        put(PLAN_CATEGORY_REPEAT, "周期性计划");
        put(PLAN_CATEGORY_CONSUME, "消费性计划");
    }};

    /**
     * 计划状态
     */
    public static final int PLAN_STATUS_STOPPED = 0;        //停止
    public static final int PLAN_STATUS_RUNNING = 1;        //运行
    public static final Map<Integer, String> SUPPORT_PLAN_STATUSES = new HashMap<Integer, String>() {{
        put(PLAN_STATUS_STOPPED, "停止");
        put(PLAN_STATUS_RUNNING, "运行");
    }};

    /**
     * 任务状态
     */
    public static final int JOB_STATUS_FINISHED = 0;        //结束
    public static final int JOB_STATUS_RUNNING = 1;         //运行
    public static final Map<Integer, String> SUPPORT_JOB_STATUSES = new HashMap<Integer, String>() {{
        put(JOB_STATUS_FINISHED, "结束");
        put(JOB_STATUS_RUNNING, "运行");
    }};

    /**
     * 数据记录类型
     */
    public static final String RECORD_TYPE_URL = "URL";
    public static final String RECORD_TYPE_FOLLOW_LINK = "FOLLOW_LINK";
    public static final String RECORD_TYPE_JOB = "JOB";
    public static final String RECORD_TYPE_PLAN = "PLAN";
    public static final String RECORD_TYPE_CONCURRENT_UNIT = "CONCURRENT_UNIT";

    /**
     * URL字段定义
     */
    public static final String KEY = "key";
    public static final String HASH = "hash";
    public static final String URL = "url";
    public static final String PARENT_URL = "parent_url";
    public static final String FETCH_CONTENT_URL = "fetch_content_url";
    public static final String FETCH_TIME = "fetch_time";
    public static final String IN_QUEUE_TIME = "in_queue_time";
    public static final String OUT_QUEUE_TIME = "out_queue_time";
    public static final String CATEGORY = "category";
    public static final String DEPTH = "depth";
    public static final String CONCURRENT_LEVEL = "concurrent_level";
    public static final String TIMEOUT = "timeout";
    public static final String HTTP_CODE = "http_code";
    public static final String FOLLOW_LINK_NUM = "follow_link_num";
    public static final String DISCARD_FOLLOW_LINK_NUM = "discard_follow_link_num";
    public static final String RESOURCE_SUFFIX = "resource_suffix";

    /**
     * 计划字段定义
     */
    public static final String PLAN_ID = "plan_id";
    public static final String APP_ID = "app_id";
    public static final String NAME = "name";
    public static final String STATUS = "status";
    public static final String PRIORITY = "priority";
    public static final String AVOID_REPEATED_FETCH = "avoid_repeated_fetch";
    public static final String CRONTAB_EXPRESSION = "crontab_expression";

    /**
     * 任务字段定义
     */
    public static final String JOB_ID = "job_id";
    public static final String BUILD_STATUS = "build_status";
    public static final String BUILD_STATUS_SUCCESS = "SUCCESS";
    public static final String BUILD_STATUS_FAIL = "FAIL";

    /**
     * 并发单元字段定义
     */
    public static final String CONCURRENT_UNIT = "concurrent_unit";
    public static final String SCHEDULE_STATUS = "schedule_status";
    public static final String SCHEDULE_STATUS_SUCCESS = "SUCCESS";
    public static final String SCHEDULE_STATUS_FAIL = "FAIL";
    public static final String INCREASE_CONNECTION_NUM = "increase_connection_num";
    public static final String ACQUIRE_CONNECTION_NUM = "acquire_connection_num";
    public static final String RELEASE_CONNECTION_NUM = "release_connection_num";
    public static final String RETURN_CONNECTION_NUM = "return_connection_num";

    /**
     * 切面日志字段定义
     */
    public static final String DARWIN_DEBUG_MESSAGE = "__DARWIN_DEBUG_MESSAGE__";
    public static final String DARWIN_STRACE_TRACE = "__DARWIN_STRACE_TRACE__";
    public static final String DARWIN_RECORD_TYPE = "__DARWIN_RECORD_TYPE__";
    public static final String DARWIN_MESSAGE_ID = "__DARWIN_MESSAGE_ID__";
    public static final String DARWIN_MESSAGE_KEY = "__DARWIN_MESSAGE_KEY__";
    public static final String DARWIN_MESSAGE_TOPIC = "__DARWIN_MESSAGE_TOPIC__";
    public static final String DARWIN_MESSAGE_TIMESTAMP = "__DARWIN_MESSAGE_TIMESTAMP__";
    public static final String DARWIN_PROCESS_TIME = "__DARWIN_PROCESS_TIME__";
    public static final String DARWIN_FETCH_TIME = "__DARWIN_FETCH_TIME__";
    public static final String DARWIN_PUT_TIME = "__DARWIN_PUT_TIME__";
    public static final String DARWIN_PARSE_TIME = "__DARWIN_PARSE_TIME__";
    public static final String DARWIN_INPUT_STREAM = "__DARWIN_INPUT_STREAM__";
}
