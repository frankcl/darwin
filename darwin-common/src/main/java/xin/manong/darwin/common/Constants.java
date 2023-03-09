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


}
