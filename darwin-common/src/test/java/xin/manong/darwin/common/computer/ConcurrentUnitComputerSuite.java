package xin.manong.darwin.common.computer;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

/**
 * @author frankcl
 * @date 2023-04-04 16:08:59
 */
public class ConcurrentUnitComputerSuite {

    @Test
    public void testCompute() {
        {
            URLRecord record = new URLRecord("http://www.sina.com.cn");
            record.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
            Assert.assertEquals("sina.com.cn", ConcurrentUnitComputer.compute(record));
        }
        {
            URLRecord record = new URLRecord("http://www.sina.com.cn");
            record.concurrentLevel = Constants.CONCURRENT_LEVEL_HOST;
            Assert.assertEquals("www.sina.com.cn", ConcurrentUnitComputer.compute(record));
        }
    }
}
