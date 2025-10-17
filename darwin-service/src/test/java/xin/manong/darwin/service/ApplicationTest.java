package xin.manong.darwin.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import xin.manong.weapon.spring.boot.annotation.EnableEtcdClient;
import xin.manong.weapon.spring.boot.annotation.EnableKafkaProducer;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;
import xin.manong.weapon.spring.boot.io.EtcdPropertySourceFactory;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@EnableRedisClient
@EnableKafkaProducer
@EnableOSSClient
@EnableEtcdClient
@PropertySource(
        name="default",
        value = "classpath:application-service-dev.yml",
        factory = EtcdPropertySourceFactory.class)
@SpringBootApplication(scanBasePackages = { "xin.manong.darwin.service", "xin.manong.darwin.queue", "xin.manong.darwin.log" })
public class ApplicationTest {
}
