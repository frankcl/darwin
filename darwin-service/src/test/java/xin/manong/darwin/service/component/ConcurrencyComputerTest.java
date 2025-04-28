package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.weapon.spring.boot.etcd.EnableWatchValueBeanProcessor;

/**
 * @author frankcl
 * @date 2025-04-28 15:27:17
 */
@RunWith(SpringRunner.class)
@EnableWatchValueBeanProcessor
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ConcurrencyComputerTest {

    @Resource
    private ConcurrencyComputer concurrencyComputer;

    @Test
    public void testCompute() {
        {
            URLRecord record = new URLRecord("https://www.sina.com.cn");
            concurrencyComputer.compute(record);
            Assert.assertEquals("sina.com.cn", record.concurrencyUnit);
            Assert.assertEquals(Constants.CONCURRENCY_LEVEL_DOMAIN, record.concurrencyLevel.intValue());
        }
        {
            URLRecord record = new URLRecord("https://www.163.com");
            concurrencyComputer.compute(record);
            Assert.assertEquals("www.163.com", record.concurrencyUnit);
            Assert.assertEquals(Constants.CONCURRENCY_LEVEL_HOST, record.concurrencyLevel.intValue());
        }
        {
            URLRecord record = new URLRecord("https://www.sohu.com");
            concurrencyComputer.compute(record);
            Assert.assertEquals("www.sohu.com", record.concurrencyUnit);
            Assert.assertEquals(Constants.CONCURRENCY_LEVEL_HOST, record.concurrencyLevel.intValue());
        }
        {
            URLRecord record = new URLRecord("https://sports.sohu.com");
            concurrencyComputer.compute(record);
            Assert.assertEquals("sohu.com", record.concurrencyUnit);
            Assert.assertEquals(Constants.CONCURRENCY_LEVEL_DOMAIN, record.concurrencyLevel.intValue());
        }
    }
}
