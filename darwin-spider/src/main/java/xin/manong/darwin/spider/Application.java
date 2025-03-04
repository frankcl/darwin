package xin.manong.darwin.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.manong.weapon.spring.boot.annotation.EnableKafkaConsumer;
import xin.manong.weapon.spring.boot.annotation.EnableKafkaProducer;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

/**
 * 应用程序入口
 * spring boot应用
 *
 * @author frankcl
 * @date 2022-08-24 12:58:39
 */
@EnableOSSClient
@EnableKafkaProducer
@EnableKafkaConsumer
@EnableRedisClient
@SpringBootApplication(scanBasePackages = {"xin.manong.darwin"})
public class Application {

    /**
     * 应用入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
