package xin.manong.darwin.common.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * 抓取/抽取链接信息
 *
 * @author frankcl
 * @date 2023-03-16 17:58:22
 */
public class LinkURL {

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
}
