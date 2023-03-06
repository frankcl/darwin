package xin.manong.darwin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 健康检测
 *
 * @author frankcl
 * @date 2022-03-11 14:12:06
 */
@RestController
@Controller
@Path("/health")
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检测
     *
     * @return 响应信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("check")
    @GetMapping("check")
    public String check() {
        return "darwin web server is ok";
    }
}
