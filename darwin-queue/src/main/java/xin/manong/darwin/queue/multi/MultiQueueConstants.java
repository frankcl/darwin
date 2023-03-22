package xin.manong.darwin.queue.multi;

/**
 * 多级队列常量定义
 *
 * @author frankcl
 * @date 2023-03-07 11:44:29
 */
public class MultiQueueConstants {

    public static final String MULTI_QUEUE_PREFIX = "DARWIN";
    public static final String MULTI_QUEUE_GLOBAL_LOCK = String.format("%s_GLOBAL_LOCK", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_HIGH_CONCURRENT_KEY_PREFIX = String.format("%s_H_CONCURRENT_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_NORMAL_CONCURRENT_KEY_PREFIX = String.format("%s_N_CONCURRENT_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_LOW_CONCURRENT_KEY_PREFIX = String.format("%s_L_CONCURRENT_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_JOB_KEY_PREFIX = String.format("%s_JOB_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_CONCURRENT_UNIT_KEY = String.format("%s_CONCURRENT_UNIT", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_JOBS_KEY = String.format("%s_JOBS", MULTI_QUEUE_PREFIX);
}
