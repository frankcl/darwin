package xin.manong.darwin.queue;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

/**
 * @author frankcl
 * @date 2023-03-14 16:59:13
 */
@EnableRedisClient
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ConcurrencyControlTest {

    @Resource
    private ConcurrencyControl concurrencyControl;

    @Test
    public void testConcurrencyControl() {
        Assert.assertEquals(3, concurrencyControl.getAvailableConnections("sohu.com"));
        Assert.assertEquals(1, concurrencyControl.getAvailableConnections("news.cn"));
        Assert.assertEquals(20, concurrencyControl.getAvailableConnections("sina.com.cn"));
        Assert.assertEquals(50, concurrencyControl.getAvailableConnections("shuwen.com"));

        concurrencyControl.putConnection("sohu.com", "aaa");
        Assert.assertEquals(2, concurrencyControl.getAvailableConnections("sohu.com"));
        concurrencyControl.putConnection("sohu.com", "bbb");
        Assert.assertEquals(1, concurrencyControl.getAvailableConnections("sohu.com"));
        concurrencyControl.putConnection("sohu.com", "ccc");
        Assert.assertEquals(0, concurrencyControl.getAvailableConnections("sohu.com"));
        concurrencyControl.putConnection("sohu.com", "ddd");
        Assert.assertEquals(0, concurrencyControl.getAvailableConnections("sohu.com"));
        Assert.assertTrue(concurrencyControl.removeConnection("sohu.com", "aaa"));
        Assert.assertTrue(concurrencyControl.removeConnection("sohu.com", "bbb"));
        Assert.assertTrue(concurrencyControl.removeConnection("sohu.com", "ccc"));
        Assert.assertEquals(2, concurrencyControl.getAvailableConnections("sohu.com"));
        Assert.assertTrue(concurrencyControl.removeConnection("sohu.com", "ddd"));
        Assert.assertEquals(3, concurrencyControl.getAvailableConnections("sohu.com"));
        Assert.assertFalse(concurrencyControl.removeConnection("sohu.com", "eee"));
        Assert.assertEquals(3, concurrencyControl.getAvailableConnections("sohu.com"));
    }
}
