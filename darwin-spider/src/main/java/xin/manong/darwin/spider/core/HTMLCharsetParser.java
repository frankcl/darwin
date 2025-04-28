package xin.manong.darwin.spider.core;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * HTML字符集解析器
 * 从HTML header部分解析字符集设置
 *
 * @author frankcl
 * @date 2025-04-11 14:53:52
 */
public class HTMLCharsetParser {

    private static final Logger logger = LoggerFactory.getLogger(HTMLCharsetParser.class);

    private static final String CSS_QUERY = "meta[http-equiv=content-type]";
    private static final String ATTR_CONTENT = "content";
    private static final String KEY_CHARSET = "charset=";

    /**
     * 从HTML header中解析字符集设置
     *
     * @param byteArray HTML字节数组
     * @return 成功返回字符集设置，否则返回null
     */
    public static String parse(byte[] byteArray) {
        Document document = Jsoup.parse(new String(byteArray, StandardCharsets.UTF_8));
        Element head = document.head();
        Elements elements = head.select(CSS_QUERY);
        for (Element element : elements) {
            if (!element.hasAttr(ATTR_CONTENT)) continue;
            String content = element.attr(ATTR_CONTENT);
            if (StringUtils.isEmpty(content)) continue;
            int index = content.indexOf(KEY_CHARSET);
            if (index == -1) continue;
            String charset = content.substring(index + KEY_CHARSET.length()).trim();
            try {
                Charset.forName(charset);
                return charset;
            } catch (UnsupportedCharsetException e) {
                logger.warn("Unsupported charset: {}", charset);
            }
        }
        return null;
    }
}
