package xin.manong.darwin.common.comparator;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

/**
 * @author frankcl
 * @date 2023-04-04 16:11:05
 */
public class URLRecordComparatorTest {

    private final URLRecordComparator comparator = new URLRecordComparator();

    @Test
    public void testCompare() {
        {
            URLRecord left = new URLRecord("http://www.sina.com.cn");
            left.priority = Constants.PRIORITY_HIGH;
            left.pushTime = 1L;
            URLRecord right = new URLRecord("http://www.sohu.com");
            right.priority = Constants.PRIORITY_NORMAL;
            right.pushTime = 1L;
            Assert.assertEquals(-1, comparator.compare(left, right));
        }
        {
            URLRecord left = new URLRecord("http://www.sina.com.cn");
            left.priority = Constants.PRIORITY_HIGH;
            left.pushTime = 1L;
            URLRecord right = new URLRecord("http://www.sohu.com");
            right.priority = Constants.PRIORITY_NORMAL;
            right.pushTime = 1L;
            Assert.assertEquals(1, comparator.compare(right, left));
        }
        {
            URLRecord left = new URLRecord("http://www.sina.com.cn");
            left.priority = Constants.PRIORITY_HIGH;
            left.pushTime = 1L;
            URLRecord right = new URLRecord("http://www.sohu.com");
            right.priority = Constants.PRIORITY_HIGH;
            right.pushTime = 2L;
            Assert.assertEquals(-1, comparator.compare(left, right));
        }
        {
            URLRecord left = new URLRecord("http://www.sina.com.cn");
            left.priority = Constants.PRIORITY_HIGH;
            left.pushTime = 1L;
            URLRecord right = new URLRecord("http://www.sohu.com");
            right.priority = Constants.PRIORITY_HIGH;
            right.pushTime = 2L;
            Assert.assertEquals(1, comparator.compare(right, left));
        }
        {
            URLRecord left = new URLRecord("http://www.sina.com.cn");
            left.priority = Constants.PRIORITY_HIGH;
            left.pushTime = 1L;
            URLRecord right = new URLRecord("http://www.sohu.com");
            right.priority = Constants.PRIORITY_HIGH;
            right.pushTime = 1L;
            Assert.assertEquals(0, comparator.compare(left, right));
        }
    }
}
