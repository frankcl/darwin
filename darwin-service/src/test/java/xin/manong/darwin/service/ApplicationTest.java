package xin.manong.darwin.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@SpringBootApplication(scanBasePackages = { "xin.manong.darwin.service" })
public class ApplicationTest {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationTest.class, args);
    }
}
