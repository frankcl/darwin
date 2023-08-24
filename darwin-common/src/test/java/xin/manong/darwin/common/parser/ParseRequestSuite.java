package xin.manong.darwin.common.parser;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

/**
 * @author frankcl
 * @date 2023-04-04 16:15:02
 */
public class ParseRequestSuite {

    @Test
    public void testCheck() {
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        record.appId = 1;
        record.jobId = "xxx";
        record.planId = "xxx";
        record.category = Constants.CONTENT_CATEGORY_LIST;
        ParseRequest request = new ParseRequest.Builder().content("xxx").record(record).build();
        Assert.assertTrue(request.check());
    }
}
