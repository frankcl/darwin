package xin.manong.darwin.common.parser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 抓取/抽取链接信息
 *
 * @author frankcl
 * @date 2023-03-16 17:58:22
 */
public class LinkURL {

    private static final Logger logger = LoggerFactory.getLogger(LinkURL.class);

    /**
     * 抓取URL
     */
    public String url;
    /**
     * HTTP头信息
     */
    public Map<String, Object> headers = new HashMap<>();
    /**
     * 用户透传数据
     */
    public Map<String, Object> userDefinedMap = new HashMap<>();

    public LinkURL(String url) {
        this.url = url;
    }

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            return false;
        }
        return true;
    }
}
