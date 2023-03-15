package xin.manong.darwin.common;

import java.util.HashSet;
import java.util.Set;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-03-06 14:27:33
 */
public class Constants {

    /**
     * URL状态
     */
    public static final int URL_STATUS_SUCCESS = 0;         //抓取成功
    public static final int URL_STATUS_FAIL = -1;           //抓取失败
    public static final int URL_STATUS_CREATED = 1;         //创建
    public static final int URL_STATUS_QUEUING = 2;         //排队中
    public static final int URL_STATUS_FETCHING = 3;        //抓取中
    public static final Set<Integer> SUPPORT_URL_STATUSES = new HashSet<Integer>() {{
        add(URL_STATUS_SUCCESS);
        add(URL_STATUS_FAIL);
        add(URL_STATUS_CREATED);
        add(URL_STATUS_QUEUING);
        add(URL_STATUS_FETCHING);
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
    public static final Set<Integer> SUPPORT_CONCURRENT_LEVELS = new HashSet<Integer>() {{
       add(CONCURRENT_LEVEL_DOMAIN);
       add(CONCURRENT_LEVEL_HOST);
    }};

    /**
     * 内容分类
     */
    public static final int CONTENT_CATEGORY_TEXT = 0;      //文本
    public static final int CONTENT_CATEGORY_RESOURCE = 1;  //资源：图片视频等
    public static final int CONTENT_CATEGORY_STREAM = 2;    //流：视频流等
    public static final Set<Integer> SUPPORT_CONTENT_CATEGORIES = new HashSet<Integer>() {{
        add(CONTENT_CATEGORY_TEXT);
        add(CONTENT_CATEGORY_RESOURCE);
        add(CONTENT_CATEGORY_STREAM);
    }};

    /**
     * 计划分类
     */
    public static final int PLAN_CATEGORY_ONCE = 0;         //一次性计划任务
    public static final int PLAN_CATEGORY_REPEAT = 1;       //周期性计划任务
    public static final int PLAN_CATEGORY_CONSUME = 2;      //消费型计划任务
    public static final Set<Integer> SUPPORT_PLAN_CATEGORIES = new HashSet<Integer>() {{
        add(PLAN_CATEGORY_ONCE);
        add(PLAN_CATEGORY_REPEAT);
        add(PLAN_CATEGORY_CONSUME);
    }};

    /**
     * 计划状态
     */
    public static final int PLAN_STATUS_STOPPED = 0;        //停止
    public static final int PLAN_STATUS_RUNNING = 1;        //运行
    public static final Set<Integer> SUPPORT_PLAN_STATUSES = new HashSet<Integer>() {{
        add(PLAN_STATUS_STOPPED);
        add(PLAN_STATUS_RUNNING);
    }};

    /**
     * 任务状态
     */
    public static final int JOB_STATUS_FINISHED = 0;        //结束
    public static final int JOB_STATUS_RUNNING = 1;        //运行
    public static final Set<Integer> SUPPORT_JOB_STATUSES = new HashSet<Integer>() {{
        add(JOB_STATUS_FINISHED);
        add(JOB_STATUS_RUNNING);
    }};


}
