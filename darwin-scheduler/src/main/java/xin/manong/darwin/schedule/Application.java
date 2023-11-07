package xin.manong.darwin.schedule;

import com.shuwen.dynamic.secret.property.ShamanPropertySourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import xin.manong.weapon.spring.boot.annotation.EnableONSProducer;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

/**
 * 应用程序入口
 * spring boot应用
 *
 * @author frankcl
 * @date 2022-08-24 12:58:39
 */
@EnableRedisClient
@EnableONSProducer
@PropertySource(name = "configmap", value = "xhzy-data#express-stream", factory = ShamanPropertySourceFactory.class)
@SpringBootApplication(scanBasePackages = {"xin.manong.darwin"})
public class Application {

    /**
     * 应用入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
