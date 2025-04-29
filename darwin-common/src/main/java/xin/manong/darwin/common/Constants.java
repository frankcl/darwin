package xin.manong.darwin.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-03-06 14:27:33
 */
public class Constants {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * URL状态
     */
    public static final int URL_STATUS_UNKNOWN = -1;                //未知
    public static final int URL_STATUS_FETCH_SUCCESS = 0;           //抓取成功
    public static final int URL_STATUS_FETCH_FAIL = 1;              //抓取失败
    public static final int URL_STATUS_QUEUING = 2;                 //排队中
    public static final int URL_STATUS_FETCHING = 3;                //抓取中
    public static final int URL_STATUS_TIMEOUT = 4;                 //抓取超时
    public static final int URL_STATUS_EXPIRED = 5;                 //过期
    public static final int URL_STATUS_ERROR = 6;                   //错误
    public static final int URL_STATUS_OVERFLOW = 7;                //溢出
    public static final Map<Integer, String> SUPPORT_URL_STATUSES = new HashMap<>() {
        {
            put(URL_STATUS_FETCH_SUCCESS, "抓取成功");
            put(URL_STATUS_FETCH_FAIL, "抓取失败");
            put(URL_STATUS_QUEUING, "排队中");
            put(URL_STATUS_FETCHING, "抓取中");
            put(URL_STATUS_TIMEOUT, "抓取超时");
            put(URL_STATUS_EXPIRED, "过期");
            put(URL_STATUS_ERROR, "错误");
            put(URL_STATUS_OVERFLOW, "溢出");
        }
    };

    /**
     * 代理类型
     */
    public static final int PROXY_CATEGORY_LONG = 1;                //长效代理
    public static final int PROXY_CATEGORY_SHORT = 2;               //短效代理
    public static final Map<Integer, String> SUPPORT_PROXY_CATEGORIES = new HashMap<>() {
        {
           put(PROXY_CATEGORY_LONG, "长效代理");
           put(PROXY_CATEGORY_SHORT, "短效代理");
        }
    };

    /**
     * 抓取方式
     */
    public static final int FETCH_METHOD_COMMON = 0;                //本地IP
    public static final int FETCH_METHOD_LONG_PROXY = 1;            //长效代理
    public static final int FETCH_METHOD_SHORT_PROXY = 2;           //短效代理
    public static final int FETCH_METHOD_RENDER = 3;                //浏览器
    public static final Map<Integer, String> SUPPORT_FETCH_METHODS = new HashMap<>() {
        {
           put(FETCH_METHOD_COMMON, "本地IP");
           put(FETCH_METHOD_LONG_PROXY, "长效代理");
           put(FETCH_METHOD_SHORT_PROXY, "短效代理");
           put(FETCH_METHOD_RENDER, "浏览器");
        }
    };

    /**
     * 数据爬取优先级
     */
    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_LOW = 2;

    /**
     * 抓取并发级别
     */
    public static final int CONCURRENCY_LEVEL_DOMAIN = 0;
    public static final int CONCURRENCY_LEVEL_HOST = 1;
    public static final Map<Integer, String> SUPPORT_CONCURRENCY_LEVELS = new HashMap<>() {
        {
            put(CONCURRENCY_LEVEL_DOMAIN, "DOMAIN");
            put(CONCURRENCY_LEVEL_HOST, "HOST");
        }
    };

    /**
     * 内容分类
     */
    public static final int CONTENT_CATEGORY_PAGE = 1;              //网页
    public static final int CONTENT_CATEGORY_RESOURCE = 2;          //资源：图片、视频和文档等
    public static final int CONTENT_CATEGORY_STREAM = 3;            //视频流
    public static final Map<Integer, String> SUPPORT_CONTENT_CATEGORIES = new HashMap<>() {
        {
            put(CONTENT_CATEGORY_PAGE, "网页");
            put(CONTENT_CATEGORY_RESOURCE, "资源");
            put(CONTENT_CATEGORY_STREAM, "视频流");
        }
    };

    /**
     * 脚本类型
     */
    public static final int SCRIPT_TYPE_GROOVY = 1;                 //Groovy脚本
    public static final int SCRIPT_TYPE_JAVASCRIPT = 2;             //JavaScript脚本
    public static final Map<Integer, String> SUPPORT_SCRIPT_TYPES = new HashMap<>() {
        {
            put(SCRIPT_TYPE_GROOVY, "Groovy");
            put(SCRIPT_TYPE_JAVASCRIPT, "JavaScript");
        }
    };

    /**
     * 抽链范围
     */
    public static final int LINK_SCOPE_ALL = 1;                     //所有
    public static final int LINK_SCOPE_DOMAIN = 2;                  //domain抽链
    public static final int LINK_SCOPE_HOST = 3;                    //host抽链
    public static final Map<Integer, String> SUPPORT_LINK_SCOPES = new HashMap<>() {
        {
            put(LINK_SCOPE_ALL, "ALL");
            put(LINK_SCOPE_DOMAIN, "DOMAIN");
            put(LINK_SCOPE_HOST, "HOST");
        }
    };

    /**
     * 计划分类
     */
    public static final int PLAN_CATEGORY_ONCE = 0;                 //单次型计划
    public static final int PLAN_CATEGORY_PERIOD = 1;               //周期型计划
    public static final Map<Integer, String> SUPPORT_PLAN_CATEGORIES = new HashMap<>() {
        {
            put(PLAN_CATEGORY_ONCE, "单次型计划");
            put(PLAN_CATEGORY_PERIOD, "周期型计划");
        }
    };

    public static final int DASHBOARD_CATEGORY_TOTAL = 1;
    public static final int DASHBOARD_CATEGORY_STATUS = 2;
    public static final int DASHBOARD_CATEGORY_CONTENT = 3;

    /**
     * 数据记录类型
     */
    public static final String RECORD_TYPE_URL = "URL";
    public static final String RECORD_TYPE_JOB = "JOB";
    public static final String RECORD_TYPE_PLAN = "PLAN";
    public static final String RECORD_TYPE_CONCURRENCY = "CONCURRENCY";

    /**
     * 数据处理阶段
     */
    public static final String PROCESS_STAGE_POP = "POP";
    public static final String PROCESS_STAGE_PUSH = "PUSH";
    public static final String PROCESS_STAGE_FETCH = "FETCH";
    public static final String PROCESS_STAGE_EXTRACT = "EXTRACT";
    public static final String PROCESS_STAGE_MONITOR = "MONITOR";

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
    public static final String CONCURRENCY_LEVEL = "concurrency_level";
    public static final String TIMEOUT = "timeout";
    public static final String HTTP_CODE = "http_code";
    public static final String CHILDREN = "children";
    public static final String INVALID_CHILDREN = "invalid_children";
    public static final String HTML_CHARSET = "html_charset";
    public static final String CHARSET = "charset";
    public static final String MEDIA_TYPE = "media_type";
    public static final String ALLOW_DISPATCH = "allow_dispatch";
    public static final String FETCHED = "fetched";

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
    public static final String CONCURRENCY_UNIT = "concurrency_unit";
    public static final String SCHEDULE_STATUS = "schedule_status";
    public static final String SCHEDULE_STATUS_SUCCESS = "SUCCESS";
    public static final String SCHEDULE_STATUS_FAIL = "FAIL";
    public static final String APPLY_RECORD_NUM = "apply_record_num";
    public static final String ALLOCATE_RECORD_NUM = "allocate_record_num";
    public static final String OVERFLOW_RECORD_NUM = "overflow_record_num";

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
