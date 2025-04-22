package xin.manong.darwin.runner.manage;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.model.Message;
import xin.manong.darwin.service.iface.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器注册管理：负责所有执行器和监控器生命周期管理
 *
 * @author frankcl
 * @date 2025-03-06 17:47:00
 */
@Component
public class ExecuteRunnerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteRunnerRegistry.class);

    @Resource
    private MessageService messageService;
    private final Map<String, ExecuteRunnerShell> executeRunnerMap = new ConcurrentHashMap<>();

    /**
     * 获取执行器信息列表
     *
     * @param runnerType 执行器类型
     * @return 执行器信息列表
     */
    public List<ExecuteRunnerMeta> getList(int runnerType) {
        List<ExecuteRunnerMeta> executeRunnerMetaList = new ArrayList<>();
        for (ExecuteRunnerShell executeRunnerShell : executeRunnerMap.values()) {
            if (executeRunnerShell.getRunnerType() != runnerType) continue;
            ExecuteRunnerMeta executeRunnerMeta = new ExecuteRunnerMeta();
            executeRunnerMeta.key = executeRunnerShell.getKey();
            executeRunnerMeta.name = executeRunnerShell.getName();
            executeRunnerMeta.description = executeRunnerShell.getDescription();
            executeRunnerMeta.status = null;
            executeRunnerMeta.messageNum = messageService.messageCount(
                    executeRunnerShell.getKey(), Message.SOURCE_TYPE_RUNNER);
            executeRunnerMetaList.add(executeRunnerMeta);
        }
        return executeRunnerMetaList;
    }

    /**
     * 注册全局执行器
     *
     * @param executeRunnerShell 全局执行器
     */
    public void register(ExecuteRunnerShell executeRunnerShell) {
        executeRunnerMap.put(executeRunnerShell.getKey(), executeRunnerShell);
    }

    /**
     * 启动执行器
     *
     * @param key 执行器key
     * @return 成功返回true，否则返回false
     */
    public boolean start(String key) {
        if (!executeRunnerMap.containsKey(key)) {
            logger.warn("Runner[{}] is not found for starting", key);
            return false;
        }
        executeRunnerMap.get(key).start();
        return true;
    }

    /**
     * 停止执行器
     *
     * @param key 执行器key
     * @return 成功返回true，否则返回false
     */
    public boolean stop(String key) {
        if (!executeRunnerMap.containsKey(key)) {
            logger.warn("Runner[{}] is not found for stopping", key);
            return false;
        }
        executeRunnerMap.get(key).stop();
        return true;
    }

    /**
     * 执行器是否运行
     *
     * @param key 执行器key
     * @return 运行返回true，否则返回false
     */
    public boolean isRunning(String key) {
        if (!executeRunnerMap.containsKey(key)) {
            logger.warn("Runner[{}] is not found for getting status", key);
            return false;
        }
        return executeRunnerMap.get(key).isRunning();
    }
}
