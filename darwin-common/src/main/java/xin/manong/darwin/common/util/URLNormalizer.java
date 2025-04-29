package xin.manong.darwin.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * URL正规化
 * 1. 去除锚点
 * 2. query重排序
 *
 * @author frankcl
 * @date 2025-04-29 19:16:12
 */
public class URLNormalizer {

    /**
     * URL规范化
     *
     * @param requestURL 请求URL
     * @return 规范化URL
     */
    public static String normalize(String requestURL) {
        try {
            URL url = new URL(requestURL);
            String query = url.getQuery();
            if (query != null) {
                String[] queries = query.split("&");
                Arrays.sort(queries);
                StringBuilder builder = new StringBuilder();
                Arrays.stream(queries).forEach(q -> {
                    if (!builder.isEmpty()) builder.append("&");
                    builder.append(q);
                });
                query = builder.toString();
            }
            StringBuilder builder = new StringBuilder();
            if (url.getProtocol() != null) builder.append(url.getProtocol());
            builder.append("://");
            if (url.getHost() != null) builder.append(url.getHost());
            if (url.getPort() != -1) builder.append(":").append(url.getPort());
            if (url.getPath() != null) builder.append(url.getPath());
            if (query != null) builder.append("?").append(query);
            return builder.toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
