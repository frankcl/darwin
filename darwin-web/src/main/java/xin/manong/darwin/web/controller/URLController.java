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
import xin.manong.darwin.service.lineage.Node;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.HTMLMender;
import xin.manong.darwin.web.component.PermissionSupport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    private static final String RESPONSE_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";

    private static final String RESPONSE_HEADER_VALUE_NO_CACHE = "no-cache";
    private static final String RESPONSE_HEADER_VALUE_ATTACHMENT = "attachment;filename=%s";

    @Resource
    private OSSService ossService;
    @Resource
    private URLService urlService;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 分发数据
     *
     * @param key 数据key
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("dispatch")
    @GetMapping("dispatch")
    public boolean dispatch(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("数据key缺失");
        URLRecord record = urlService.get(key);
        if (record == null) throw new NotFoundException("数据不存在");
        if (record.status != Constants.URL_STATUS_FETCH_SUCCESS) throw new IllegalStateException("数据抓取失败");
        permissionSupport.checkAppPermission(record.appId);
        return urlService.dispatch(record) != null;
    }

    /**
     * 根据key获取URL记录
     *
     * @param key 数据key
     * @return URL记录
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public URLRecord get(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("数据key缺失");
        URLRecord record = urlService.get(key);
        if (record == null) throw new NotFoundException("数据不存在");
        record.mimeType = record.mediaType == null ? null : record.mediaType.toString();
        return record;
    }

    /**
     * 删除数据
     *
     * @param key 数据Key
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @GetMapping("delete")
    public Boolean delete(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("数据key缺失");
        URLRecord record = urlService.get(key);
        if (record == null) throw new NotFoundException("抓取数据不存在");
        permissionSupport.checkAppPermission(record.appId);
        return urlService.delete(key);
    }

    /**
     * 根据key获取血统节点
     *
     * @param key 数据key
     * @return 血统节点
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getLineageNode")
    @GetMapping("getLineageNode")
    public Node getLineageNode(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("数据key缺失");
        return urlService.getLineageNode(key);
    }

    /**
     * 根据父key获取血统节点列表
     *
     * @param parentKey 父数据key
     * @return 血统节点列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getLineageChildren")
    @GetMapping("getLineageChildren")
    public List<Node> getLineageChildren(@QueryParam("parent_key") String parentKey) {
        if (StringUtils.isEmpty(parentKey)) throw new BadRequestException("父数据key缺失");
        return urlService.getLineageChildren(parentKey);
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
    public String previewHTML(@QueryParam("key") String key) throws IOException {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("预览数据key缺失");
        URLRecord record = urlService.get(key);
        checkPreviewRecord(record);
        if (!xin.manong.darwin.common.model.MediaType.TEXT_HTML.equals(record.mediaType) &&
                !xin.manong.darwin.common.model.MediaType.APPLICATION_XHTML.equals(record.mediaType)) {
            throw new UnsupportedOperationException("抓取结果不是HTML");
        }
        byte[] byteArray = ossService.getByURL(record.fetchContentURL);
        String html = new String(byteArray, StandardCharsets.UTF_8);
        html = HTMLMender.amendContentType(html);
        html = HTMLMender.amendReferrer(html);
        return HTMLMender.amendBaseURL(html, new URL(StringUtils.isEmpty(record.redirectURL) ?
                record.url : record.redirectURL));
    }

    /**
     * 预览数据流
     *
     * @param key 数据key
     * @return 响应
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("previewStream")
    @GetMapping("previewStream")
    public Response previewStream(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("预览数据key缺失");
        try {
            URLRecord record = urlService.get(key);
            checkPreviewRecord(record);
            if (!xin.manong.darwin.common.model.MediaType.APPLICATION_PDF.equals(record.mediaType) &&
                    !record.mediaType.isAudio()) {
                throw new IllegalStateException("抓取结果不是音频或PDF文档");
            }
            InputStream input = ossService.getObjectStream(record.fetchContentURL);
            StreamingOutput output = outputStream -> {
                input.transferTo(outputStream);
                outputStream.flush();
                input.close();
            };
            String attachment = "preview";
            if (record.mediaType != null && StringUtils.isNotEmpty(record.mediaType.suffix)) {
                attachment = String.format("%s.%s", attachment, record.mediaType.suffix);
            }
            return Response.ok(output).header(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_HEADER_VALUE_NO_CACHE).
                    header(RESPONSE_HEADER_CONTENT_DISPOSITION, String.format(RESPONSE_HEADER_VALUE_ATTACHMENT,
                            attachment)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
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
    public String preview(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("预览数据key缺失");
        URLRecord record = urlService.get(key);
        checkPreviewRecord(record);
        if (record.mediaType.isImage() || record.mediaType.isVideo() || record.mediaType.isAudio()) {
            return ossService.signURL(record.fetchContentURL);
        }
        if (record.mediaType.equals(xin.manong.darwin.common.model.MediaType.APPLICATION_JSON) ||
                record.mediaType.equals(xin.manong.darwin.common.model.MediaType.APPLICATION_XML) ||
                record.mediaType.equals(xin.manong.darwin.common.model.MediaType.APPLICATION_JAVASCRIPT) ||
                record.mediaType.equals(xin.manong.darwin.common.model.MediaType.APPLICATION_X_JAVASCRIPT) ||
                record.mediaType.equals(xin.manong.darwin.common.model.MediaType.APPLICATION_XHTML) ||
                record.mediaType.isText()) {
            byte[] byteArray = ossService.getByURL(record.fetchContentURL);
            return new String(byteArray, StandardCharsets.UTF_8);
        }
        throw new UnsupportedOperationException("媒体类型不支持预览");
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
    public Pager<URLRecord> search(@BeanParam URLSearchRequest request) {
        Pager<URLRecord> pager = urlService.search(request);
        for (URLRecord record : pager.records) {
            record.mimeType = record.mediaType == null ? null : record.mediaType.toString();
        }
        return pager;
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
        return Response.ok(output).header(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_HEADER_VALUE_NO_CACHE).
                header(RESPONSE_HEADER_CONTENT_DISPOSITION, String.format(RESPONSE_HEADER_VALUE_ATTACHMENT,
                        "export.xlsx")).build();
    }

    /**
     * 检测预览数据
     *
     * @param record URL数据
     */
    private void checkPreviewRecord(URLRecord record) {
        if (record == null) throw new NotFoundException("预览数据不存在");
        if (record.httpCode != 200) throw new IllegalStateException("抓取失败");
        if (!ossService.existsByURL(record.fetchContentURL)) throw new IllegalStateException("数据失效");
    }
}
