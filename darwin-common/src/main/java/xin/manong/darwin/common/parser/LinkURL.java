package xin.manong.darwin.common.parser;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.aliyun.ots.annotation.Column;

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
     * 超时时间（毫秒）
     */
    public Integer timeout = 5000;

    /**
     * 优先级
     */
    public Integer priority = Constants.PRIORITY_NORMAL;

    /**
     * 抓取方式
     */
    public Integer fetchMethod = Constants.FETCH_METHOD_COMMON;
    /**
     * 抓取URL类型
     */
    public Integer category = Constants.CONTENT_CATEGORY_TEXT;
    /**
     * 抓取并发级别
     */
    public Integer concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
    /**
     * 抓取URL
     */
    public String url;
    /**
     * HTTP头信息
     */
    public Map<String, String> headers = new HashMap<>();
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
