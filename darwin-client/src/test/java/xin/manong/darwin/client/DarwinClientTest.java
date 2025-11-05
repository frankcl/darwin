package xin.manong.darwin.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xin.manong.darwin.common.request.PlanExecuteRequest;
import xin.manong.darwin.common.request.SeedRequest;

import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2025-11-05 17:29:22
 */
public class DarwinClientTest {

    private DarwinClient client;

    @Before
    public void setUp() {
        DarwinClientConfig config = new DarwinClientConfig();
        config.serverURL = "https://darwin.lumychip.com";
        client = new DarwinClient(config);
    }

    @Test
    public void testPlanSubmit() {
        PlanExecuteRequest request = new PlanExecuteRequest();
        request.planId = "85a444d43a204165b08b9cea168dce4f";
        request.accessKey = "HuFFLh3D39Jo";
        request.secretKey = "235b688264905caf8c5db413de1baf2d";
        request.seeds = new ArrayList<>();
        SeedRequest seedRequest = new SeedRequest();
        seedRequest.url = "http://nil.csail.mit.edu/6.824/2022/papers/mapreduce.pdf";
        seedRequest.allowDispatch = true;
        seedRequest.customMap.put("test", 123);
        seedRequest.customMap.put("abc", "456");
        request.seeds.add(seedRequest);
        Assert.assertTrue(client.planSubmit(request));
    }
}
