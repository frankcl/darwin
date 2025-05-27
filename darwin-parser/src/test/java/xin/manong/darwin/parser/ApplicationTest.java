package xin.manong.darwin.parser;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import xin.manong.weapon.spring.boot.annotation.EnableEtcdClient;
import xin.manong.weapon.spring.boot.annotation.EnableKafkaProducer;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;
import xin.manong.weapon.spring.boot.io.EtcdPropertySourceFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 应用测试入口
 *
 * @author frankcl
 * @date 2022-08-15 21:08:20
 */
@EnableEtcdClient
@EnableRedisClient
@EnableKafkaProducer
@EnableOSSClient
@PropertySource(
        name="default",
        value = "application-service-dev.yml",
        factory = EtcdPropertySourceFactory.class)
@SpringBootApplication(scanBasePackages = {
        "xin.manong.darwin.parser", "xin.manong.darwin.service", "xin.manong.darwin.queue", "xin.manong.darwin.log" })
public class ApplicationTest {

    public static String readScript(String path) throws Exception {
        int n, size = 4096;
        byte[] byteArray = new byte[size];
        try (InputStream inputStream = ApplicationTest.class.getResourceAsStream(path);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            assert inputStream != null;
            while ((n = inputStream.read(byteArray, 0, size)) != -1) {
                outputStream.write(byteArray, 0, n);
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }
}
