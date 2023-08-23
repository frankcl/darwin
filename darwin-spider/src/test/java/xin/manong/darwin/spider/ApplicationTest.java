package xin.manong.darwin.spider;

import com.shuwen.dynamic.secret.property.ShamanPropertySourceFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import xin.manong.weapon.spring.boot.annotation.EnableONSProducer;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@EnableRedisClient
@EnableONSProducer
@EnableOSSClient
@EnableAutoConfiguration
@PropertySource(name = "configmap", value = "xhzy-data#express-stream", factory = ShamanPropertySourceFactory.class)
@SpringBootApplication(scanBasePackages = { "xin.manong.darwin.spider", "xin.manong.darwin.service",
        "xin.manong.darwin.queue", "xin.manong.darwin.parse" })
public class ApplicationTest {
}
