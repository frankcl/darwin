package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.service.request.ProxySearchRequest;
import xin.manong.darwin.spider.proxy.SingleProxySelector;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.ProxyRequest;
import xin.manong.darwin.web.request.ProxyUpdateRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpClientConfig;
import xin.manong.weapon.base.http.HttpProxyAuthenticator;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.io.IOException;

/**
 * 代理控制器
 *
 * @author frankcl
 * @date 2023-12-12 14:56:21
 */
@RestController
@Controller
@Path("/api/proxy")
@RequestMapping("/api/proxy")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Resource
    private ProxyService proxyService;
    @Resource
    private PermissionSupport permissionSupport;
    private final HttpProxyAuthenticator authenticator;

    public ProxyController() {
        authenticator = new HttpProxyAuthenticator();
    }

    /**
     * 检测代理有效性
     *
     * @param id 代理ID
     * @return 有效返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("check")
    @GetMapping("check")
    @EnableWebLogAspect
    public boolean check(@QueryParam("id") Integer id) throws IOException {
        if (id == null) throw new BadRequestException("代理ID缺失");
        Proxy proxy = proxyService.get(id);
        if (proxy == null) throw new NotFoundException("代理未找到");
        String requestURL = "https://darwin.manong.xin/api/health/check";
        SingleProxySelector proxySelector = new SingleProxySelector(proxy);
        HttpClient httpClient = new HttpClient(new HttpClientConfig(), proxySelector, authenticator);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, null);
        try (Response httpResponse = httpClient.execute(httpRequest)) {
            if (!httpResponse.isSuccessful()) {
                logger.error("Check proxy failed for {}, http code:{}", proxy, httpResponse.code());
                return false;
            }
        }
        return true;
    }

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
        if (id == null) throw new BadRequestException("代理ID缺失");
        return proxyService.get(id);
    }

    /**
     * 搜索代理
     *
     * @param request 代理搜索请求
     * @return 代理分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<Proxy> search(@BeanParam ProxySearchRequest request) {
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
    public Boolean add(@RequestBody ProxyRequest request) {
        if (request == null) throw new BadRequestException("代理请求信息为空");
        request.check();
        permissionSupport.checkAdmin();
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
    public Boolean update(@RequestBody ProxyUpdateRequest request) {
        if (request == null) throw new BadRequestException("代理更新信息为空");
        request.check();
        permissionSupport.checkAdmin();
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
        if (id == null) throw new BadRequestException("代理ID为空");
        permissionSupport.checkAdmin();
        return proxyService.delete(id);
    }
}
