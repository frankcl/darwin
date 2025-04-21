package xin.manong.darwin.runner.manage;

import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * etcd数据变换回调处理
 *
 * @author frankcl
 * @date 2024-11-12 15:10:39
 */
public class LockPathRemoveConsumer implements Consumer<WatchResponse> {

    private static final Logger logger = LoggerFactory.getLogger(LockPathRemoveConsumer.class);

    private final ExecuteRunnerShell executeRunner;

    public LockPathRemoveConsumer(ExecuteRunnerShell executeRunner) {
        this.executeRunner = executeRunner;
    }

    @Override
    public void accept(WatchResponse watchResponse) {
        List<WatchEvent> watchEvents = watchResponse.getEvents();
        for (WatchEvent watchEvent : watchEvents) {
            KeyValue keyValue = watchEvent.getKeyValue();
            String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
            WatchEvent.EventType eventType = watchEvent.getEventType();
            if (eventType == WatchEvent.EventType.DELETE) {
                logger.warn("Lock path:{} is removed", key);
                if (executeRunner != null) executeRunner.asyncStop();
            }
        }
    }
}
