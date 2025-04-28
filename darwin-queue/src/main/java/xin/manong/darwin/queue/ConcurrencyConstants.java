package xin.manong.darwin.queue;

/**
 * 多级队列常量定义
 *
 * @author frankcl
 * @date 2023-03-07 11:44:29
 */
public class ConcurrencyConstants {

    /**
     * 内存水位等级定义
     */
    public static final int MEMORY_WATER_LEVEL_NORMAL = 0;
    public static final int MEMORY_WATER_LEVEL_WARNING = 1;
    public static final int MEMORY_WATER_LEVEL_DANGER = 2;

    /**
     * Redis键常量定义
     */
    public static final String CONCURRENCY_PREFIX = "DARWIN_CONCURRENCY";
    public static final String CONCURRENCY_CONTROL = "DARWIN_CONTROL";
    public static final String CONCURRENCY_PUSH_LOCK = String.format("%s_PUSH_LOCK", CONCURRENCY_PREFIX);
    public static final String CONCURRENCY_POP_LOCK = String.format("%s_POP_LOCK", CONCURRENCY_PREFIX);
    public static final String CONCURRENCY_HIGH_PRIORITY = String.format("%s_HIGH_PRIORITY_", CONCURRENCY_PREFIX);
    public static final String CONCURRENCY_NORMAL_PRIORITY = String.format("%s_NORMAL_PRIORITY_", CONCURRENCY_PREFIX);
    public static final String CONCURRENCY_LOW_PRIORITY = String.format("%s_LOW_PRIORITY_", CONCURRENCY_PREFIX);
    public static final String CONCURRENCY_UNITS = String.format("%s_UNITS", CONCURRENCY_PREFIX);
}
