package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.ExcelBuilder;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

/**
 * URL链接控制器
 *
 * @author frankcl
 * @date 2023-04-24 14:44:36
 */
@RestController
@Controller
@Path("/url")
@RequestMapping("/url")
public class URLController {

    private static final Logger logger = LoggerFactory.getLogger(URLController.class);

    private static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    private static final String CACHE_CONTROL_VALUE_NO_CACHE = "no-cache";
    private static final String CONTENT_DISPOSITION_VALUE = "attachment;filename=export.xlsx";

    @Resource
    protected URLService urlService;

    /**
     * 根据key获取URL记录
     *
     * @param key url key
     * @return URL记录
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public URLRecord get(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) {
            logger.error("key is empty");
            throw new BadRequestException("key缺失");
        }
        return urlService.get(key);
    }

    /**
     * 搜索URL记录
     *
     * @param request 搜索请求
     * @return URL分页列表
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<URLRecord> search(URLSearchRequest request) {
        if (request == null) request = new URLSearchRequest();
        if (request.current == null || request.current < 1) request.current = Constants.DEFAULT_CURRENT;
        if (request.size == null || request.size <= 0) request.size = Constants.DEFAULT_PAGE_SIZE;
        return urlService.search(request);
    }

    /**
     * 导出URL
     *
     * @param request 搜索请求
     * @return 响应
     * @throws IOException
     */
    @POST
    @Path("export")
    @PostMapping("export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response export(URLSearchRequest request) throws IOException {
        if (request == null) request = new URLSearchRequest();
        ExcelBuilder builder = urlService.export(request);
        if (builder == null) throw new RuntimeException("导出数据失败");
        StreamingOutput output = outputStream -> {
            builder.export(outputStream);
            outputStream.flush();
        };
        return Response.ok(output).
                header(HEADER_CACHE_CONTROL, CACHE_CONTROL_VALUE_NO_CACHE).
                header(HEADER_CONTENT_DISPOSITION, CONTENT_DISPOSITION_VALUE).build();
    }
}
