package xin.manong.darwin.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器注册管理：负责所有执行器和监控器生命周期管理
 *
 * @author frankcl
 * @date 2025-03-06 17:47:00
 */
@Component
public class GlobalExecutorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExecutorRegistry.class);

    private final Map<String, GlobalExecutor> globalExecuteRunnerMap = new ConcurrentHashMap<>();

    /**
     * 注册全局执行器
     *
     * @param globalExecutor 全局执行器
     */
    public void register(GlobalExecutor globalExecutor) {
        globalExecuteRunnerMap.put(globalExecutor.getName(), globalExecutor);
    }

    /**
     * 启动执行器
     *
     * @param name 执行器名称
     * @return 成功返回true，否则返回false
     */
    public boolean start(String name) {
        if (!globalExecuteRunnerMap.containsKey(name)) {
            logger.warn("global executor[{}] is not found for starting", name);
            return false;
        }
        globalExecuteRunnerMap.get(name).start();
        return true;
    }

    /**
     * 停止执行器
     *
     * @param name 执行器名称
     * @return 成功返回true，否则返回false
     */
    public boolean stop(String name) {
        if (!globalExecuteRunnerMap.containsKey(name)) {
            logger.warn("global executor[{}] is not found for stopping", name);
            return false;
        }
        globalExecuteRunnerMap.get(name).stop();
        return true;
    }
}
