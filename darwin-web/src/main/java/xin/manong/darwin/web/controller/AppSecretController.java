package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.AppSecret;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.AppSecretService;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.request.AppSecretSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.AppSecretRequest;
import xin.manong.darwin.web.request.AppSecretUpdateRequest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * 应用秘钥控制器
 *
 * @author frankcl
 * @date 2025-10-16 16:20:25
 */
@RestController
@Controller
@Path("/api/app_secret")
@RequestMapping("/api/app_secret")
public class AppSecretController {

    private static final Logger logger = LoggerFactory.getLogger(AppSecretController.class);

    @Resource
    private AppService appService;
    @Resource
    private AppSecretService appSecretService;
    @Resource
    private PermissionSupport permissionSupport;
    @Resource
    private ExecutorService executorService;

    /**
     * 根据ID获取应用秘钥
     *
     * @param id 秘钥ID
     * @return 应用秘钥
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public AppSecret get(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("应用秘钥ID为空");
        AppSecret appSecret = appSecretService.get(id);
        if (appSecret == null) throw new NotFoundException("应用秘钥不存在");
        return appSecret;
    }

    /**
     * 添加应用秘钥
     *
     * @param request 添加请求
     * @return 成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    public boolean add(@RequestBody AppSecretRequest request) {
        if (request == null) throw new BadRequestException("应用秘钥添加请求为空");
        request.check();
        permissionSupport.checkAdmin();
        AppSecret appSecret = Converter.convert(request);
        return appSecretService.add(appSecret);
    }

    /**
     * 更新应用秘钥
     *
     * @param request 更新请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public boolean update(@RequestBody AppSecretUpdateRequest request) {
        if (request == null) throw new BadRequestException("应用秘钥更新请求为空");
        request.check();
        permissionSupport.checkAdmin();
        AppSecret appSecret = Converter.convert(request);
        return appSecretService.update(appSecret);
    }

    /**
     * 删除应用秘钥
     *
     * @param id 秘钥ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    public boolean delete(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("删除秘钥ID为空");
        permissionSupport.checkAdmin();
        return appSecretService.delete(id);
    }

    /**
     * 搜索应用秘钥
     *
     * @param searchRequest 搜索请求
     * @return 应用秘钥分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    public Pager<AppSecret> search(@BeanParam AppSecretSearchRequest searchRequest) {
        if (searchRequest == null) throw new BadRequestException("应用秘钥搜索请求为空");
        Pager<AppSecret> pager = appSecretService.search(searchRequest);
        if (pager.records != null) {
            CountDownLatch countDownLatch = new CountDownLatch(pager.records.size());
            pager.records.forEach(appSecret -> {
                executorService.submit(() -> {
                    try {
                        App app = appService.get(appSecret.appId);
                        if (app != null) appSecret.appName = app.name;
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            });
            try {
                countDownLatch.await();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return pager;
    }

    /**
     * 生成随机AccessKey
     *
     * @return 随机AccessKey
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("randomAccessKey")
    @GetMapping("randomAccessKey")
    public String randomAccessKey() {
        return appSecretService.randomAccessKey();
    }

    /**
     * 生成随机SecretKey
     *
     * @return 随机SecretKey
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("randomSecretKey")
    @GetMapping("randomSecretKey")
    public String randomSecretKey() {
        return appSecretService.randomSecretKey();
    }
}
