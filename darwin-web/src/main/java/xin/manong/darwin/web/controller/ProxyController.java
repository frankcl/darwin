package xin.manong.darwin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.service.request.ProxySearchRequest;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.ProxyRequest;
import xin.manong.darwin.web.request.ProxyUpdateRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 代理控制器
 *
 * @author frankcl
 * @date 2023-12-12 14:56:21
 */
@RestController
@Controller
@Path("/proxy")
@RequestMapping("/proxy")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Resource
    protected ProxyService proxyService;

    /**
     * 根据ID获取代理
     *
     * @param id 代理ID
     * @return 代理信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public Proxy get(@QueryParam("id") Integer id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("代理ID缺失");
        }
        return proxyService.get(id);
    }

    /**
     * 搜索代理
     *
     * @param request 代理搜索请求
     * @return 代理分页列表
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<Proxy> search(ProxySearchRequest request) {
        if (request == null) request = new ProxySearchRequest();
        if (request.current == null || request.current < 1) request.current = Constants.DEFAULT_CURRENT;
        if (request.size == null || request.size <= 0) request.size = Constants.DEFAULT_PAGE_SIZE;
        return proxyService.search(request);
    }

    /**
     * 添加代理
     *
     * @param request 代理信息
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(ProxyRequest request) {
        if (request == null) {
            logger.error("proxy request is null");
            throw new BadRequestException("代理请求信息为空");
        }
        request.check();
        Proxy proxy = Converter.convert(request);
        return proxyService.add(proxy);
    }

    /**
     * 更新代理
     *
     * @param request 代理更新信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public Boolean update(ProxyUpdateRequest request) {
        if (request == null) {
            logger.error("proxy update info is null");
            throw new BadRequestException("代理更新信息为空");
        }
        request.check();
        if (proxyService.get(request.id) == null) {
            logger.error("proxy[{}] is not found", request.id);
            throw new NotFoundException(String.format("代理[%d]不存在", request.id));
        }
        Proxy proxy = Converter.convert(request);
        return proxyService.update(proxy);
    }

    /**
     * 删除代理
     *
     * @param id 代理ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") Integer id) {
        if (id == null) {
            logger.error("proxy id is null");
            throw new BadRequestException("代理ID为空");
        }
        if (proxyService.get(id) == null) {
            logger.error("proxy[{}] is not found", id);
            throw new NotFoundException(String.format("代理[%d]不存在", id));
        }
        return proxyService.delete(id);
    }
}
