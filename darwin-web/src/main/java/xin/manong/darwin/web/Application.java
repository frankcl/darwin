package xin.manong.darwin.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.manong.hylian.client.annotation.EnableCORSFilter;
import xin.manong.hylian.client.annotation.EnableHylianGuard;
import xin.manong.weapon.spring.boot.annotation.EnableEtcdClient;
import xin.manong.weapon.spring.boot.annotation.EnableJettySupportSSL;
import xin.manong.weapon.spring.boot.annotation.EnableKafkaProducer;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

/**
 * 应用程序入口
 * spring boot应用
 *
 * @author frankcl
 * @date 2022-08-24 12:58:39
 */
@EnableEtcdClient
@EnableRedisClient
@EnableJettySupportSSL
@EnableKafkaProducer
@EnableOSSClient
@EnableWebLogAspect
@EnableHylianGuard
@EnableCORSFilter
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
