package xin.manong.darwin.spider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-08-23 14:15:41
 */
@ActiveProfiles(value = { "dev", "service", "service-dev", "parse", "parse-dev", "queue", "queue-dev", "log", "log-dev" })
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
public class SpiderFactorySuite {

    @Resource
    protected SpiderFactory spiderFactory;

    @Test
    public void testBuild() {
        {
            URLRecord record = new URLRecord();
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            Spider spider = spiderFactory.build(record);
            Assert.assertTrue(spider != null && spider instanceof HTMLSpider);
        }
        {
            URLRecord record = new URLRecord();
            record.category = Constants.CONTENT_CATEGORY_LIST;
            Spider spider = spiderFactory.build(record);
            Assert.assertTrue(spider != null && spider instanceof HTMLSpider);
        }
        {
            URLRecord record = new URLRecord();
            record.category = Constants.CONTENT_CATEGORY_RESOURCE;
            Spider spider = spiderFactory.build(record);
            Assert.assertTrue(spider != null && spider instanceof ResourceSpider);
        }
        {
            URLRecord record = new URLRecord();
            record.category = Constants.CONTENT_CATEGORY_STREAM;
            Spider spider = spiderFactory.build(record);
            Assert.assertTrue(spider != null && spider instanceof StreamSpider);
        }
    }
}
