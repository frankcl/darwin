package xin.manong.darwin.common;

import java.io.Serial;
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

    public static final int DEFAULT_CURRENT = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 支持MimeType
     */
    public static final Set<String> SUPPORT_MIME_TYPES = new HashSet<>() {
        @Serial
        private static final long serialVersionUID = -4264962171854052054L;

        {
        add("text");
        add("image");
        add("video");
        add("application");
    }};

    /**
     * URL状态
     */
    public static final int URL_STATUS_SUCCESS = 0;                 //成功
    public static final int URL_STATUS_CREATED = 1;                 //创建
    public static final int URL_STATUS_QUEUING_REFUSED = 2;         //排队拒绝
    public static final int URL_STATUS_QUEUING = 3;                 //排队中
    public static final int URL_STATUS_FETCHING = 4;                //抓取中
    public static final int URL_STATUS_INVALID = 5;                 //URL非法
    public static final int URL_STATUS_TIMEOUT = 6;                 //超时
    public static final int URL_STATUS_FETCH_FAIL = 7;              //抓取失败
    public static final int URL_STATUS_OVERFLOW = 8;                //溢出
    public static final Map<Integer, String> SUPPORT_URL_STATUSES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -897638930689419495L;

        {
        put(URL_STATUS_SUCCESS, "成功");
        put(URL_STATUS_CREATED, "创建");
        put(URL_STATUS_QUEUING_REFUSED, "排队拒绝");
        put(URL_STATUS_QUEUING, "排队中");
        put(URL_STATUS_FETCHING, "抓取中");
        put(URL_STATUS_INVALID, "非法状态");
        put(URL_STATUS_TIMEOUT, "超时");
        put(URL_STATUS_FETCH_FAIL, "抓取失败");
        put(URL_STATUS_OVERFLOW, "溢出");
    }};

    /**
     * 代理类型
     */
    public static final int PROXY_CATEGORY_LONG = 1;                //长效代理
    public static final int PROXY_CATEGORY_SHORT = 2;               //短效代理
    public static final Map<Integer, String> SUPPORT_PROXY_CATEGORIES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -7228170191562416106L;

        {
       put(PROXY_CATEGORY_LONG, "长效代理");
       put(PROXY_CATEGORY_SHORT, "短效代理");
    }};

    /**
     * 抓取方式
     */
    public static final int FETCH_METHOD_COMMON = 0;                //普通抓取
    public static final int FETCH_METHOD_LONG_PROXY = 1;            //长效代理抓取
    public static final int FETCH_METHOD_SHORT_PROXY = 2;           //短效代理抓取
    public static final int FETCH_METHOD_RENDER = 3;                //渲染
    public static final Map<Integer, String> SUPPORT_FETCH_METHODS = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -2431279279613319588L;

        {
       put(FETCH_METHOD_COMMON, "普通抓取");
       put(FETCH_METHOD_LONG_PROXY, "长效代理抓取");
       put(FETCH_METHOD_SHORT_PROXY, "短效代理抓取");
       put(FETCH_METHOD_RENDER, "渲染");
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
    public static final Map<Integer, String> SUPPORT_CONCURRENT_LEVELS = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -9008342954794992607L;

        {
       put(CONCURRENT_LEVEL_DOMAIN, "DOMAIN");
       put(CONCURRENT_LEVEL_HOST, "HOST");
    }};

    /**
     * 内容分类
     */
    public static final int CONTENT_CATEGORY_CONTENT = 1;           //内容页
    public static final int CONTENT_CATEGORY_LIST = 2;              //列表页
    public static final int CONTENT_CATEGORY_RESOURCE = 3;          //资源：图片视频等
    public static final int CONTENT_CATEGORY_STREAM = 4;            //视频流等
    public static final Map<Integer, String> SUPPORT_CONTENT_CATEGORIES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 3318891098314553137L;

        {
        put(CONTENT_CATEGORY_CONTENT, "内容页");
        put(CONTENT_CATEGORY_LIST, "列表页");
        put(CONTENT_CATEGORY_RESOURCE, "图片视频");
        put(CONTENT_CATEGORY_STREAM, "视频流");
    }};

    /**
     * 脚本类型
     */
    public static final int SCRIPT_TYPE_GROOVY = 1;                 //Groovy脚本
    public static final int SCRIPT_TYPE_JAVASCRIPT = 2;             //JavaScript脚本
    public static final Map<Integer, String> SUPPORT_SCRIPT_TYPES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -1339559361036477136L;

        {
        put(SCRIPT_TYPE_GROOVY, "Groovy");
        put(SCRIPT_TYPE_JAVASCRIPT, "JavaScript");
    }};

    /**
     * 抽链范围
     */
    public static final int LINK_SCOPE_ALL = 1;                     //全局抽链
    public static final int LINK_SCOPE_DOMAIN = 2;                  //domain抽链
    public static final int LINK_SCOPE_HOST = 3;                    //host抽链
    public static final Map<Integer, String> SUPPORT_LINK_SCOPES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -4319598110652058246L;

        {
        put(LINK_SCOPE_ALL, "ALL");
        put(LINK_SCOPE_DOMAIN, "DOMAIN");
        put(LINK_SCOPE_HOST, "HOST");
    }};

    /**
     * 计划分类
     */
    public static final int PLAN_CATEGORY_ONCE = 0;                 //单次型计划
    public static final int PLAN_CATEGORY_PERIOD = 1;               //周期型计划
    public static final Map<Integer, String> SUPPORT_PLAN_CATEGORIES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -3162511278902980623L;

        {
        put(PLAN_CATEGORY_ONCE, "单次型计划");
        put(PLAN_CATEGORY_PERIOD, "周期型计划");
    }};

    /**
     * 执行器状态
     */
    public static final int EXECUTOR_STATUS_STOPPED = 0;
    public static final int EXECUTOR_STATUS_RUNNING = 1;
    public static final int EXECUTOR_STATUS_ERROR = 2;
    public static final Map<Integer, String> SUPPORT_EXECUTOR_STATUSES = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 1999022686678355828L;

        {
            put(EXECUTOR_STATUS_STOPPED, "停止");
            put(EXECUTOR_STATUS_RUNNING, "运行");
            put(EXECUTOR_STATUS_ERROR, "错误");
        }};

    /**
     * 数据记录类型
     */
    public static final String RECORD_TYPE_URL = "URL";
    public static final String RECORD_TYPE_JOB = "JOB";
    public static final String RECORD_TYPE_PLAN = "PLAN";
    public static final String RECORD_TYPE_CONCURRENT_UNIT = "CONCURRENT_UNIT";

    /**
     * 数据处理阶段
     */
    public static final String STAGE_POP = "POP";
    public static final String STAGE_PUSH = "PUSH";
    public static final String STAGE_FETCH = "FETCH";
    public static final String STAGE_EXTRACT = "EXTRACT";
    public static final String STAGE_MONITOR = "MONITOR";

    /**
     * URL字段定义
     */
    public static final String KEY = "key";
    public static final String HASH = "hash";
    public static final String URL = "url";
    public static final String PARENT_URL = "parent_url";
    public static final String REDIRECT_URL = "redirect_url";
    public static final String FETCH_CONTENT_URL = "fetch_content_url";
    public static final String FETCH_TIME = "fetch_time";
    public static final String FETCH_METHOD = "fetch_method";
    public static final String PUSH_TIME = "push_time";
    public static final String POP_TIME = "pop_time";
    public static final String CATEGORY = "category";
    public static final String DEPTH = "depth";
    public static final String CONCURRENT_LEVEL = "concurrent_level";
    public static final String TIMEOUT = "timeout";
    public static final String HTTP_CODE = "http_code";
    public static final String CHILDREN = "children";
    public static final String INVALID_CHILDREN = "invalid_children";
    public static final String MIME_TYPE = "mime_type";
    public static final String SUB_MIME_TYPE = "sub_mime_type";
    public static final String CHARSET = "charset";

    /**
     * 计划字段定义
     */
    public static final String PLAN_ID = "plan_id";
    public static final String APP_ID = "app_id";
    public static final String NAME = "name";
    public static final String STATUS = "status";
    public static final String PRIORITY = "priority";
    public static final String ALLOW_REPEAT = "allow_repeat";
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
    public static final String APPLIED_CONNECTION_NUM = "applied_connection_num";
    public static final String ACQUIRED_CONNECTION_NUM = "acquired_connection_num";
    public static final String OVERFLOW_CONNECTION_NUM = "overflow_connection_num";

    /**
     * 切面日志字段定义
     */
    public static final String DARWIN_DEBUG_MESSAGE = "__DARWIN_DEBUG_MESSAGE__";
    public static final String DARWIN_STACK_TRACE = "__DARWIN_STACK_TRACE__";
    public static final String DARWIN_STAGE = "__DARWIN_STAGE__";
    public static final String DARWIN_RECORD_TYPE = "__DARWIN_RECORD_TYPE__";
    public static final String DARWIN_MESSAGE_ID = "__DARWIN_MESSAGE_ID__";
    public static final String DARWIN_MESSAGE_KEY = "__DARWIN_MESSAGE_KEY__";
    public static final String DARWIN_MESSAGE_TOPIC = "__DARWIN_MESSAGE_TOPIC__";
    public static final String DARWIN_MESSAGE_TIMESTAMP = "__DARWIN_MESSAGE_TIMESTAMP__";
    public static final String DARWIN_PROCESS_TIME = "__DARWIN_PROCESS_TIME__";
    public static final String DARWIN_FETCH_TIME = "__DARWIN_FETCH_TIME__";
    public static final String DARWIN_WRITE_TIME = "__DARWIN_WRITE_TIME__";
    public static final String DARWIN_PARSE_TIME = "__DARWIN_PARSE_TIME__";
}
