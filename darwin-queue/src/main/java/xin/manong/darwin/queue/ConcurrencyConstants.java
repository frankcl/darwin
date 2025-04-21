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
    public static final int MEMORY_WATER_LEVEL_UNKNOWN = -1;
    public static final int MEMORY_WATER_LEVEL_NORMAL = 0;
    public static final int MEMORY_WATER_LEVEL_WARNING = 1;
    public static final int MEMORY_WATER_LEVEL_DANGER = 2;

    /**
     * Redis键常量定义
     */
    public static final String CONCURRENT_PREFIX = "DARWIN_CONCURRENT";
    public static final String CONCURRENT_CONTROL = "DARWIN_CONTROL";
    public static final String CONCURRENT_PUSH_LOCK = String.format("%s_PUSH_LOCK", CONCURRENT_PREFIX);
    public static final String CONCURRENT_POP_LOCK = String.format("%s_POP_LOCK", CONCURRENT_PREFIX);
    public static final String CONCURRENT_HIGH_PRIORITY = String.format("%s_HIGH_PRIORITY_", CONCURRENT_PREFIX);
    public static final String CONCURRENT_NORMAL_PRIORITY = String.format("%s_NORMAL_PRIORITY_", CONCURRENT_PREFIX);
    public static final String CONCURRENT_LOW_PRIORITY = String.format("%s_LOW_PRIORITY_", CONCURRENT_PREFIX);
    public static final String CONCURRENT_UNITS = String.format("%s_UNITS", CONCURRENT_PREFIX);
}
