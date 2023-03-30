package xin.manong.darwin.spider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.weapon.spring.boot.annotation.EnableONSProducer;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-03-30 15:50:07
 */
@EnableRedisClient
@EnableONSProducer
@EnableOSSClient
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "dev", "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ResourceSpiderSuite {

    @Resource
    protected ResourceSpider spider;

    @Test
    public void testSpider() {

    }
}
