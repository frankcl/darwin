package xin.manong.darwin.parse.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.manong.weapon.spring.boot.annotation.EnableONSProducer;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@EnableRedisClient
@EnableONSProducer
@SpringBootApplication(scanBasePackages = { "xin.manong.darwin.parse", "xin.manong.darwin.service",
        "xin.manong.darwin.queue", "xin.manong.darwin.log" })
public class ApplicationTest {
}
