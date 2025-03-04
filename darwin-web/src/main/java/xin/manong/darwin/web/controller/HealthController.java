package xin.manong.darwin.web.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return "darwin server is ok";
    }
}
