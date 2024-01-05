package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RuleUser;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.RuleUserService;
import xin.manong.darwin.service.request.RuleUserSearchRequest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-10-20 13:41:36
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class RuleUserServiceImplSuite {

    @Resource
    protected RuleUserService ruleUserService;

    @Test
    @Transactional
    @Rollback
    public void testRuleUserOperations() {
        List<Integer> ids = new ArrayList<>();
        {
            RuleUser ruleUser = new RuleUser();
            ruleUser.ruleId = 1;
            ruleUser.userId = "user1";
            ruleUser.userRealName = "frankcl";
            Assert.assertTrue(ruleUserService.add(ruleUser));
            ids.add(ruleUser.id);
        }
        {
            RuleUser ruleUser = new RuleUser();
            ruleUser.ruleId = 2;
            ruleUser.userId = "user2";
            ruleUser.userRealName = "jack";
            Assert.assertTrue(ruleUserService.add(ruleUser));
            ids.add(ruleUser.id);
        }
        Assert.assertTrue(ruleUserService.hasRulePermission("user1", 1));
        Assert.assertTrue(ruleUserService.hasRulePermission("user2", 2));
        Assert.assertFalse(ruleUserService.hasRulePermission("user2", 1));
        {
            RuleUserSearchRequest searchRequest = new RuleUserSearchRequest();
            searchRequest.ruleId = 1;
            Pager<RuleUser> pager = ruleUserService.search(searchRequest);
            Assert.assertEquals(1, pager.total.intValue());
            Assert.assertEquals("user1", pager.records.get(0).userId);
        }
        for (Integer id : ids) Assert.assertTrue(ruleUserService.delete(id));
    }
}
