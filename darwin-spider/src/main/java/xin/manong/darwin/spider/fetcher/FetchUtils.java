package xin.manong.darwin.spider.fetcher;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.URLRecord;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 抓取工具
 *
 * @author frankcl
 * @date 2026-04-27 13:49:44
 */
public class FetchUtils {

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment";
    private static final String CONTENT_DISPOSITION_FILENAME = "filename";
    private static final String CONTENT_TYPE_CHARSET = "charset";

    /**
     * 通过HTTP响应头判断是否为下载附件
     *
     * @param headers HTTP响应头
     * @return 是返回true，否则返回false
     */
    public static boolean isAttachment(Map<String, String> headers) {
        Map<String, String> itemMap = parseContentDisposition(headers);
        return itemMap.containsKey(CONTENT_DISPOSITION_ATTACHMENT);
    }

    /**
     * 获取文件后缀
     * 1. 从Content-Disposition中获取文件后缀
     * 2. 从URL中获取文件后缀
     *
     * @param record 数据
     * @return 文件后缀
     */
    public static String getSuffix(URLRecord record) {
        String suffix = getSuffixFromContentDisposition(record);
        return StringUtils.isEmpty(suffix) ? getSuffixFromURL(record) : suffix;
    }

    /**
     * 从Content-Disposition中获取文件后缀
     *
     * @param record 数据
     * @return 文件后缀
     */
    public static String getSuffixFromContentDisposition(URLRecord record) {
        Map<String, String> itemMap = parseContentDisposition(record.getHeaders());
        if (!itemMap.containsKey(CONTENT_DISPOSITION_FILENAME)) return null;
        String filename = itemMap.get(CONTENT_DISPOSITION_FILENAME);
        int pos = filename.lastIndexOf(".");
        return pos == -1 ? "" : filename.substring(pos + 1).toLowerCase();
    }

    /**
     * 通过URL路径获取文件后缀
     *
     * @param record 数据
     * @return 文件后缀，不存在返回null
     */
    public static String getSuffixFromURL(URLRecord record) {
        try {
            URL url = new URL(record.url);
            String path = url.getPath();
            if (StringUtils.isEmpty(path)) return null;
            int pos = path.lastIndexOf(".");
            return pos == -1 ? null : path.substring(pos + 1).toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析HTTP响应头Content-Disposition
     * 无法解析返回空Map
     *
     * @param headers HTTP响应头
     * @return 返回Map形式的Content-Disposition
     */
    public static Map<String, String> parseContentDisposition(Map<String, String> headers) {
        Map<String, String> itemMap = new HashMap<>();
        if (headers == null || !headers.containsKey(HEADER_CONTENT_DISPOSITION) ||
                !headers.containsKey(HEADER_CONTENT_DISPOSITION.toLowerCase())) return itemMap;
        String contentDisposition = headers.get(HEADER_CONTENT_DISPOSITION);
        if (StringUtils.isEmpty(contentDisposition)) {
            contentDisposition = headers.get(HEADER_CONTENT_DISPOSITION.toLowerCase());
        }
        if (StringUtils.isEmpty(contentDisposition)) return itemMap;
        String[] items = contentDisposition.trim().split(";");
        for (String item : items) {
            item = item.trim();
            if (StringUtils.isEmpty(item)) continue;
            int pos = item.indexOf("=");
            if (pos == -1) {
                item = item.trim().toLowerCase();
                itemMap.put(item, item);
            } else {
                String value = item.substring(pos + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                itemMap.put(item.substring(0, pos).trim().toLowerCase(), value);
            }
        }
        return itemMap;
    }

    /**
     * 解析媒体类型
     *
     * @param headers HTTP响应头
     * @return 媒体类型
     */
    public static MediaType parseMediaType(Map<String, String> headers) {
        if (headers == null || !headers.containsKey(HEADER_CONTENT_TYPE) ||
                !headers.containsKey(HEADER_CONTENT_TYPE.toLowerCase())) return null;
        String contentType = headers.get(HEADER_CONTENT_TYPE);
        if (StringUtils.isEmpty(contentType)) contentType = headers.get(HEADER_CONTENT_TYPE.toLowerCase());
        if (StringUtils.isEmpty(contentType)) return null;
        String[] parts = contentType.split(";");
        String mimeType = parts[0].trim();
        int pos = mimeType.indexOf("/");
        String type = pos == -1 ? mimeType : mimeType.substring(0, pos).trim();
        String subType = pos == -1 ? null : mimeType.substring(pos + 1).trim();
        MediaType mediaType = new MediaType(type, subType);
        for (int i = 1; i < parts.length; i++) {
            pos = parts[i].indexOf("=");
            if (pos == -1) continue;
            String key = parts[i].substring(0, pos).trim();
            String value = parts[i].substring(pos + 1).trim();
            if (!key.equalsIgnoreCase(CONTENT_TYPE_CHARSET)) continue;
            mediaType.charset = value;
            if ((mediaType.charset.startsWith("\"") && mediaType.charset.endsWith("\"")) ||
                    (mediaType.charset.startsWith("'") && mediaType.charset.endsWith("'"))) {
                mediaType.charset = mediaType.charset.substring(1, mediaType.charset.length() - 1);
            }
        }
        return mediaType;
    }
}
