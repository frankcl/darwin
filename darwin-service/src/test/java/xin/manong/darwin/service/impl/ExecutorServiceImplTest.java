package xin.manong.darwin.service.impl;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Executor;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.ExecutorService;

import java.util.List;

/**
 * @author frankcl
 * @date 2025-03-09 14:04:49
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ExecutorServiceImplTest {

    @Resource
    protected ExecutorService executorService;

    @Test
    @Transactional
    @Rollback
    public void testExecutorOperations() {
        Executor executor = new Executor();
        executor.name = "测试执行器1";
        executor.status = Constants.EXECUTOR_STATUS_STOPPED;
        Assert.assertTrue(executorService.add(executor));
        Assert.assertTrue(executor.id != null && executor.id > 0);

        Executor updateExecutor = new Executor();
        updateExecutor.id = executor.id;
        updateExecutor.name = "测试执行器";
        updateExecutor.status = Constants.EXECUTOR_STATUS_RUNNING;
        Assert.assertTrue(executorService.update(updateExecutor));

        Executor getExecutor = executorService.get("测试执行器");
        Assert.assertNotNull(getExecutor);
        Assert.assertEquals("测试执行器", getExecutor.name);
        Assert.assertEquals(Constants.EXECUTOR_STATUS_RUNNING, getExecutor.status.intValue());

        List<Executor> executors = executorService.getList();
        Assert.assertEquals(1, executors.size());
        Assert.assertEquals(executor.id, executors.get(0).id);
    }
}
