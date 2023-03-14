package xin.manong.darwin.queue.concurrent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.darwin.queue.ApplicationTest;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-03-14 16:59:13
 */
@EnableRedisClient
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ConcurrentManagerSuite {

    @Resource
    private ConcurrentManager concurrentManager;

    @Test
    public void testConcurrentManager() {
        Assert.assertEquals(3, concurrentManager.getAvailableConnections("sohu.com"));
        Assert.assertEquals(1, concurrentManager.getAvailableConnections("news.cn"));
        Assert.assertEquals(200, concurrentManager.getAvailableConnections("sina.com.cn"));
        Assert.assertEquals(50, concurrentManager.getAvailableConnections("shuwen.com"));

        Assert.assertEquals(1, concurrentManager.increaseConnections("news.cn", 1));
        Assert.assertEquals(0, concurrentManager.getAvailableConnections("news.cn"));
        Assert.assertEquals(0, concurrentManager.increaseConnections("news.cn", 1));
        Assert.assertEquals(0, concurrentManager.getAvailableConnections("news.cn"));
        Assert.assertEquals(1, concurrentManager.decreaseConnections("news.cn", 2));
        Assert.assertEquals(1, concurrentManager.getAvailableConnections("news.cn"));

        Assert.assertEquals(0, concurrentManager.decreaseConnections("sina.com.cn", 201));
        Assert.assertEquals(200, concurrentManager.getAvailableConnections("sina.com.cn"));
        Assert.assertEquals(200, concurrentManager.increaseConnections("sina.com.cn", 201));
        Assert.assertEquals(0, concurrentManager.getAvailableConnections("sina.com.cn"));
        Assert.assertEquals(200, concurrentManager.decreaseConnections("sina.com.cn", 300));
        Assert.assertEquals(200, concurrentManager.getAvailableConnections("sina.com.cn"));
    }
}
