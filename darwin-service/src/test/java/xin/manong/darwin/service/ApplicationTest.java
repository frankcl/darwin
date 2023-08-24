package xin.manong.darwin.service;

import com.shuwen.dynamic.secret.property.ShamanPropertySourceFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource(name = "configmap", value = "xhzy-data#express-stream", factory = ShamanPropertySourceFactory.class)
@SpringBootApplication(scanBasePackages = { "xin.manong.darwin.service", "xin.manong.darwin.queue", "xin.manong.darwin.log" })
public class ApplicationTest {
}
