package xin.manong.darwin.service.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * HTML修正：用于预览HTML修正
 *
 * @author frankcl
 * @date 2025-04-14 16:50:41
 */
public class HTMLMender {

    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";

    private static final String ELEMENT_NAME_BASE = "base";
    private static final String ELEMENT_NAME_META = "meta";
    private static final String ATTR_NAME_HREF = "href";
    private static final String ATTR_NAME_HTTP_EQUIV = "http-equiv";
    private static final String ATTR_NAME_NAME = "name";
    private static final String ATTR_NAME_CONTENT = "content";
    private static final String ATTR_VALUE_REFERRER = "referrer";
    private static final String ATTR_VALUE_CONTENT_TYPE = "Content-Type";
    private static final String ATTR_VALUE_NO_REFERRER = "no-referrer";
    private static final String ATTR_VALUE_TEXT_HTML_UTF8 = "text/html; charset=utf-8";

    /**
     * 修复Content-Type为text/html; charset-utf8
     *
     * @param html HTML字符串
     * @return 修正后HTML
     */
    public static String amendContentType(String html) {
        Document document = Jsoup.parse(html);
        Element head = document.head();
        Elements elements = head.selectXpath(ELEMENT_NAME_META);
        for (Element element : elements) {
            if (!element.attr(ATTR_NAME_HTTP_EQUIV).equals(ATTR_VALUE_CONTENT_TYPE)) continue;
            element.remove();
        }
        Element element = head.appendElement(ELEMENT_NAME_META);
        element.attr(ATTR_NAME_HTTP_EQUIV, ATTR_VALUE_CONTENT_TYPE);
        element.attr(ATTR_NAME_CONTENT, ATTR_VALUE_TEXT_HTML_UTF8);
        return document.html();
    }

    /**
     * 修正referrer策略为no-referrer
     *
     * @param html HTML字符串
     * @return 修正后HTML
     */
    public static String amendReferrer(String html) {
        Document document = Jsoup.parse(html);
        Element head = document.head();
        Elements elements = head.selectXpath(ELEMENT_NAME_META);
        for (Element element : elements) {
            if (!element.attr(ATTR_NAME_NAME).equals(ATTR_VALUE_REFERRER)) continue;
            element.remove();
        }
        Element element = head.appendElement(ELEMENT_NAME_META);
        element.attr(ATTR_NAME_NAME, ATTR_VALUE_REFERRER);
        element.attr(ATTR_NAME_CONTENT, ATTR_VALUE_NO_REFERRER);
        return document.html();
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
