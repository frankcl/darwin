package xin.manong.darwin.queue;

/**
 * 多级队列常量定义
 *
 * @author frankcl
 * @date 2023-03-07 11:44:29
 */
public class MultiQueueConstants {

    public static final String MULTI_QUEUE_PREFIX = "DARWIN";
    public static final String MULTI_QUEUE_HIGH_HOST_KEY_PREFIX = String.format("%s_H_HOST_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_NORMAL_HOST_KEY_PREFIX = String.format("%s_N_HOST_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_LOW_HOST_KEY_PREFIX = String.format("%s_L_NORMAL_HOST_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_JOB_KEY_PREFIX = String.format("%s_JOB_", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_HOSTS_KEY = String.format("%s_HOSTS", MULTI_QUEUE_PREFIX);
    public static final String MULTI_QUEUE_JOBS_KEY = String.format("%s_JOBS", MULTI_QUEUE_PREFIX);
}
