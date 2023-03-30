package xin.manong.darwin.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@SpringBootApplication(scanBasePackages = { "xin.manong.darwin.spider", "xin.manong.darwin.service",
        "xin.manong.darwin.queue", "xin.manong.darwin.parse" })
public class ApplicationTest {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationTest.class, args);
    }
}
