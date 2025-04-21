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
import xin.manong.darwin.common.model.Message;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.MessageService;

/**
 * @author frankcl
 * @date 2025-03-09 14:04:49
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class MessageServiceImplTest {

    @Resource
    protected MessageService messageService;

    @Test
    @Transactional
    @Rollback
    public void testMessageOperations() {
        Message message = new Message();
        message.sourceKey = "测试执行器1";
        message.sourceType = 1;
        message.message = "test";
        Assert.assertTrue(messageService.push(message));
        Assert.assertTrue(message.id != null && message.id > 0);

        Assert.assertEquals(1L, messageService.messageCount("测试执行器1", 1).longValue());

        message = messageService.pop("测试执行器1", 1);
        Assert.assertNotNull(message);
        Assert.assertEquals("测试执行器1", message.sourceKey);
        Assert.assertEquals(1, message.sourceType.intValue());
        Assert.assertEquals("test", message.message);
        Assert.assertEquals(0L, messageService.messageCount("测试执行器1", 1).longValue());
    }
}
