package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

/**
 * @author frankcl
 * @date 2023-03-06 18:22:07
 */
public class PlanTest {

    @Test
    public void testCheck() throws Exception {
        Plan plan = new Plan();
        plan.appId = 1;
        plan.appName = "test";
        plan.planId = "test_id";
        plan.name = "test_job";
        plan.status = false;
        plan.category = Constants.PLAN_CATEGORY_ONCE;
        plan.priority = Constants.PRIORITY_HIGH;
        Assert.assertTrue(plan.check());
    }
}
