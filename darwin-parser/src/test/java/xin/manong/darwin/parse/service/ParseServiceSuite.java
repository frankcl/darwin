package xin.manong.darwin.parse.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-04-04 18:00:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parse", "parse-dev", "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class ParseServiceSuite {

    @Resource
    protected ParseService parseService;

    @Test
    public void testParseWithScriptText() {
        System.out.println();
    }
}
