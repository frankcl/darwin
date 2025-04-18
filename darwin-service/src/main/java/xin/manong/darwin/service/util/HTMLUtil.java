package xin.manong.darwin.service.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

import java.net.URL;

/**
 * HTML工具
 *
 * @author frankcl
 * @date 2025-04-14 16:50:41
 */
public class HTMLUtil {

    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";

    private static final String ELEMENT_NAME_BASE = "base";
    private static final String ATTR_NAME_HREF = "href";

    private static final String MIME_TYPE_TEXT = "text";
    private static final String MIME_TYPE_APPLICATION = "application";
    private static final String SUB_MIME_TYPE_PDF = "pdf";
    private static final String SUB_MIME_TYPE_JSON = "json";
    private static final String SUB_MIME_TYPE_HTML = "html";

    /**
     * 根据资源mimeType构建资源文件后缀
     *
     * @param record URL记录
     * @return 成功返回资源文件后缀，否则返回null
     */
    public static String generateSuffixUsingMimeType(URLRecord record) {
        if (!Constants.SUPPORT_MIME_TYPES.contains(record.mimeType)) return null;
        if (StringUtils.isEmpty(record.subMimeType)) return null;
        if (record.mimeType.equalsIgnoreCase(MIME_TYPE_TEXT) &&
                !record.subMimeType.equalsIgnoreCase(SUB_MIME_TYPE_HTML)) return null;
        if (record.mimeType.equalsIgnoreCase(MIME_TYPE_APPLICATION) &&
                !record.subMimeType.equalsIgnoreCase(SUB_MIME_TYPE_PDF) &&
                !record.subMimeType.equalsIgnoreCase(SUB_MIME_TYPE_JSON)) return null;
        return record.subMimeType.toLowerCase();
    }

    /**
     * 为HTML修正基准URL
     * 1. 如缺失则添加base元素
     * 2. 如存在base元素则修正base元素href为绝对路径
     *
     * @param html HTML字符串
     * @param baseURL 基准URL
     * @return 修正后HTML
     */
    public static String amendBaseURL(String html, URL baseURL) {
        Document document = Jsoup.parse(html);
        Element head = document.head();
        Elements elements = head.selectXpath(ELEMENT_NAME_BASE);
        if (elements.isEmpty()) {
            Element element = head.appendElement(ELEMENT_NAME_BASE);
            element.attr(ATTR_NAME_HREF, baseURL.toString());
            return document.html();
        }
        for (Element element : elements) {
            String href = element.attr(ATTR_NAME_HREF);
            if (href.startsWith(HTTP_PROTOCOL) || href.startsWith(HTTPS_PROTOCOL)) continue;
            int port = baseURL.getPort();
            String protocol = baseURL.getProtocol();
            String path = baseURL.getPath();
            int index = path.lastIndexOf("/");
            if (index != -1) path = path.substring(0, index + 1);
            if (path.isEmpty()) path += "/";
            String prefixURL = String.format("%s://%s", protocol, baseURL.getHost());
            if (port != -1) prefixURL += ":" + port;
            if (href.startsWith("/")) element.attr(ATTR_NAME_HREF, String.format("%s%s", prefixURL, href));
            else element.attr(ATTR_NAME_HREF, String.format("%s%s%s", prefixURL, path, href));
        }
        return document.html();
    }
}
