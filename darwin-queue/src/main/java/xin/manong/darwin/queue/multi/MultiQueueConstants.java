package xin.manong.darwin.queue.multi;

/**
 * 多级队列常量定义
 *
 * @author frankcl
 * @date 2023-03-07 11:44:29
 */
public class MultiQueueConstants {

    /**
     * 队列内存级别
     */
    public static final int MULTI_QUEUE_MEMORY_LEVEL_NORMAL = 0;
    public static final int MULTI_QUEUE_MEMORY_LEVEL_WARN = 1;
    public static final int MULTI_QUEUE_MEMORY_LEVEL_REFUSED = 2;

    public static final String MULTI_QUEUE_PREFIX = "DARWIN";
    public static final String MULTI_QUEUE_IN_QUEUE_LOCK = String.format("%s_IN_QUEUE_LOCK", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_OUT_QUEUE_LOCK = String.format("%s_OUT_QUEUE_LOCK", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_HIGH_CONCURRENT_UNIT_QUEUE = String.format("%s_H_QUEUE_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_NORMAL_CONCURRENT_UNIT_QUEUE = String.format("%s_N_QUEUE_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_LOW_CONCURRENT_UNIT_QUEUE = String.format("%s_L_QUEUE_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_JOB_RECORD = String.format("%s_JOB_RECORD_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_CONCURRENT_UNITS = String.format("%s_CONCURRENT_UNITS", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_JOBS = String.format("%s_JOBS", MULTI_QUEUE_PREFIX);
}
