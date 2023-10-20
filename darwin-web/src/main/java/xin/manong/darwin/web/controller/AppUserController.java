package xin.manong.darwin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.service.request.AppUserSearchRequest;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.AppUserRequest;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 应用用户关系控制器
 *
 * @author frankcl
 * @date 2023-10-20 10:29:52
 */
@RestController
@Controller
@Path("/app_user")
@RequestMapping("/app_user")
public class AppUserController {

    private static final Logger logger = LoggerFactory.getLogger(AppUserController.class);

    @Resource
    protected AppUserService appUserService;

    /**
     * 获取应用用户列表
     *
     * @param appId 应用ID
     * @param current 页码，从1开始
     * @param size 分页大小，默认20
     * @return 应用用户关系分页数据
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppUsers")
    @GetMapping("getAppUsers")
    public Pager<AppUser> getAppUsers(@QueryParam("app_id") Long appId,
                                      @QueryParam("current") Integer current,
                                      @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = Constants.DEFAULT_CURRENT;
        if (size == null || size <= 0) size = Constants.DEFAULT_PAGE_SIZE;
        if (appId == null) {
            logger.error("app id is null");
            throw new BadRequestException("应用ID为空");
        }
        AppUserSearchRequest searchRequest = new AppUserSearchRequest();
        searchRequest.current = current;
        searchRequest.size = size;
        searchRequest.appId = appId;
        return appUserService.search(searchRequest);
    }


    /**
     * 添加应用用户关系
     *
     * @param request 应用用户关系
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    public Boolean add(AppUserRequest request) {
        if (request == null) {
            logger.error("app user relation is null");
            throw new BadRequestException("应用用户关系为空");
        }
        request.check();
        AppUser appUser = Converter.convert(request);
        return appUserService.add(appUser);
    }

    /**
     * 删除应用用户关系
     *
     * @param id 应用用户关系ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    public Boolean delete(@QueryParam("id") Long id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("应用用户关系ID缺失");
        }
        if (appUserService.get(id) == null) {
            logger.error("app user relation is not found for id[{}]", id);
            throw new NotFoundException(String.format("应用用户关系[%d]不存在", id));
        }
        return appUserService.delete(id);
    }
}
