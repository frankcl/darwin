package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-04-04 15:51:00
 */
public class AppTest {

    @Test
    public void testCheckOK() {
        App app = new App();
        app.name = "test";
        Assert.assertTrue(app.check());
    }

    @Test
    public void testCheckError() {
        App app = new App();
        Assert.assertFalse(app.check());
    }
}
