package xin.manong.darwin.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import xin.manong.weapon.spring.boot.annotation.*;
import xin.manong.weapon.spring.boot.etcd.EnableWatchValueBeanProcessor;
import xin.manong.weapon.spring.boot.io.EtcdPropertySourceFactory;

/**
 * 应用程序入口
 * spring boot应用
 *
 * @author frankcl
 * @date 2022-08-24 12:58:39
 */
@EnableOSSClient
@EnableEtcdClient
@EnableRocketMQConsumer
@EnableRocketMQProducer
@EnableRedisClient
@EnableWatchValueBeanProcessor
@PropertySource(
        name="default",
        value = "application-service-${spring.profiles.active}.yml",
        factory = EtcdPropertySourceFactory.class)
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
