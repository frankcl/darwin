package xin.manong.darwin.service.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.aliyun.ots.OTSSearchResponse;
import xin.manong.weapon.base.record.KVRecord;
import xin.manong.weapon.base.record.KVRecords;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-04-04 10:46:54
 */
public class ConverterTest {

    @Test
    public void testConvertSQLPage() {
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        List<URLRecord> records = new ArrayList<>();
        records.add(record);
        Page<URLRecord> page = new Page<>(1, 1, 1);
        page.setRecords(records);

        Pager<URLRecord> pager = Converter.convert(page);
        Assert.assertNotNull(pager);
        Assert.assertEquals(1, pager.pageNum.intValue());
        Assert.assertEquals(1, pager.pageSize.intValue());
        Assert.assertEquals(1L, pager.total.longValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals("http://www.sina.com.cn", pager.records.get(0).url);
        Assert.assertNotNull(pager.records.get(0).key);
        Assert.assertEquals(DigestUtils.md5Hex("http://www.sina.com.cn"), pager.records.get(0).hash);
    }

    @Test
    public void testConvertOTSSearchResponse() {
        KVRecords kvRecords = new KVRecords();
        {
            KVRecord kvRecord = new KVRecord();
            kvRecord.setKeys(new HashSet<>() {
                @Serial
                private static final long serialVersionUID = 2780345494822902156L;

                { add(Constants.KEY); }});
            kvRecord.put(Constants.URL, "http://www.sina.com.cn");
            kvRecord.put(Constants.KEY, "test_key");
            kvRecord.put(Constants.HASH, DigestUtils.md5Hex("http://www.sina.com.cn"));
            kvRecords.addRecord(kvRecord);
        }
        OTSSearchResponse response = OTSSearchResponse.buildOK(kvRecords, 1L);
        Pager<URLRecord> pager = Converter.convert(response, URLRecord.class, 1, 1);
        Assert.assertNotNull(pager);
        Assert.assertEquals(1, pager.pageNum.intValue());
        Assert.assertEquals(1, pager.pageSize.intValue());
        Assert.assertEquals(1L, pager.total.longValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals("http://www.sina.com.cn", pager.records.get(0).url);
        Assert.assertEquals("test_key", pager.records.get(0).key);
        Assert.assertEquals(DigestUtils.md5Hex("http://www.sina.com.cn"), pager.records.get(0).hash);
    }
}
