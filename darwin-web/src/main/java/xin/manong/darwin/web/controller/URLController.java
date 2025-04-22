package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.ExcelDocumentExporter;
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.HTMLUtil;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * URL控制器
 *
 * @author frankcl
 * @date 2023-04-24 14:44:36
 */
@RestController
@Controller
@Path("/api/url")
@RequestMapping("/api/url")
public class URLController {

    private static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    private static final String CACHE_CONTROL_VALUE_NO_CACHE = "no-cache";
    private static final String CONTENT_DISPOSITION_VALUE = "attachment;filename=export.xlsx";

    @Resource
    protected OSSService ossService;
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
        if (StringUtils.isEmpty(key)) throw new BadRequestException("数据key缺失");
        URLRecord record = urlService.get(key);
        if (record == null) throw new NotFoundException("数据不存在");
        return record;
    }

    /**
     * 预览抓取结果
     *
     * @return 预览结果URL
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("previewHTML")
    @GetMapping("previewHTML")
    @EnableWebLogAspect
    public String previewHTML(@QueryParam("key") String key) throws IOException {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("预览数据key缺失");
        URLRecord record = urlService.get(key);
        checkPreviewRecord(record);
        MediaType mediaType = new MediaType(record.mimeType, record.subMimeType);
        if ((record.category != Constants.CONTENT_CATEGORY_CONTENT &&
                record.category != Constants.CONTENT_CATEGORY_LIST) ||
                mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            throw new UnsupportedOperationException("抓取结果不是HTML");
        }
        byte[] byteArray = ossService.getByURL(record.fetchContentURL);
        String html = new String(byteArray, StandardCharsets.UTF_8);
        return HTMLUtil.amendBaseURL(html, new URL(StringUtils.isEmpty(record.redirectURL) ?
                record.url : record.redirectURL));
    }

    /**
     * 预览抓取结果
     *
     * @return 预览结果URL
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("preview")
    @GetMapping("preview")
    @EnableWebLogAspect
    public String preview(@QueryParam("key") String key) throws IOException {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("预览数据key缺失");
        URLRecord record = urlService.get(key);
        checkPreviewRecord(record);
        if (record.category == Constants.CONTENT_CATEGORY_STREAM ||
                record.category == Constants.CONTENT_CATEGORY_RESOURCE) {
            return ossService.signURL(record.fetchContentURL);
        }
        MediaType mediaType = new MediaType(record.mimeType, record.subMimeType);
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            byte[] byteArray = ossService.getByURL(record.fetchContentURL);
            return new String(byteArray, StandardCharsets.UTF_8);
        }
        throw new UnsupportedOperationException("HTML数据请调用previewHTML方法");
    }

    /**
     * 搜索URL记录
     *
     * @param request 搜索请求
     * @return URL分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<URLRecord> search(@BeanParam URLSearchRequest request) {
        return urlService.search(request);
    }

    /**
     * 导出URL
     *
     * @param request 搜索请求
     * @return 响应
     * @throws IOException I/O异常
     */
    @GET
    @Path("export")
    @GetMapping("export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response export(@BeanParam URLSearchRequest request) throws IOException {
        ExcelDocumentExporter exporter = urlService.export(request);
        if (exporter == null) throw new InternalServerErrorException("导出数据失败");
        StreamingOutput output = outputStream -> {
            exporter.export(outputStream);
            outputStream.flush();
        };
        return Response.ok(output).
                header(HEADER_CACHE_CONTROL, CACHE_CONTROL_VALUE_NO_CACHE).
                header(HEADER_CONTENT_DISPOSITION, CONTENT_DISPOSITION_VALUE).build();
    }

    /**
     * 检测预览数据
     *
     * @param record URL数据
     */
    private void checkPreviewRecord(URLRecord record) {
        if (record == null) throw new NotFoundException("预览数据不存在");
        if (record.status != Constants.URL_STATUS_SUCCESS ||
                !ossService.existsByURL(record.fetchContentURL)) {
            throw new IllegalStateException("数据抓取失败，不支持预览");
        }
    }
}
