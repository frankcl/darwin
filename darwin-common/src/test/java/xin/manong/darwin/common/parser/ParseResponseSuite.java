package xin.manong.darwin.common.parser;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.model.URLRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author frankcl
 * @date 2023-04-04 16:17:40
 */
public class ParseResponseSuite {

    @Test
    public void testBuildStructureResponse() {
        Map<String, Object> structureMap = new HashMap<>();
        structureMap.put("k1", "v1");
        structureMap.put("k2", 123L);
        Map<String, Object> userDefinedMap = new HashMap<>();
        userDefinedMap.put("k1", "abc");
        userDefinedMap.put("k2", 100L);
        ParseResponse response = ParseResponse.buildStructureResponse(structureMap, userDefinedMap);
        Assert.assertTrue(response.status);
        Assert.assertEquals(2, response.structureMap.size());
        Assert.assertTrue(response.structureMap.containsKey("k1"));
        Assert.assertTrue(response.structureMap.containsKey("k2"));
        Assert.assertEquals("v1", response.structureMap.get("k1"));
        Assert.assertEquals(123L, ((Long) response.structureMap.get("k2")).longValue());
        Assert.assertEquals(2, response.userDefinedMap.size());
        Assert.assertTrue(response.userDefinedMap.containsKey("k1"));
        Assert.assertTrue(response.userDefinedMap.containsKey("k2"));
        Assert.assertEquals("abc", response.userDefinedMap.get("k1"));
        Assert.assertEquals(100L, ((Long) response.userDefinedMap.get("k2")).longValue());
    }

    @Test
    public void testBuildFollowLinkResponse() {
        List<URLRecord> followLinks = new ArrayList<>();
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        followLinks.add(record);
        ParseResponse response = ParseResponse.buildFollowLinkResponse(followLinks);
        Assert.assertTrue(response.status);
        Assert.assertTrue(response.followLinks != null && !response.followLinks.isEmpty());
        Assert.assertEquals(1, response.followLinks.size());
        Assert.assertEquals("http://www.sina.com.cn", response.followLinks.get(0).url);
    }

    @Test
    public void testBuildErrorResponse() {
        ParseResponse response = ParseResponse.buildErrorResponse("Error");
        Assert.assertFalse(response.status);
        Assert.assertEquals("Error", response.message);
    }
}
