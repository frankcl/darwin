package xin.manong.darwin.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.manong.weapon.spring.boot.annotation.EnableOSSClient;

/**
 * 应用程序入口
 * spring boot应用
 *
 * @author frankcl
 * @date 2022-08-24 12:58:39
 */
@EnableOSSClient
@SpringBootApplication(scanBasePackages = {"xin.manong.darwin", "xin.manong.weapon.spring.web.ws.aspect"})
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
