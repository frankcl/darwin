package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求URL
 *
 * @author frankcl
 * @date 2023-11-07 10:52:10
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(URLRequest.class);

    /**
     * 超时时间（毫秒）
     */
    @JsonProperty("timeout")
    public Integer timeout;

    /**
     * 优先级
     * 高优先级：0
     * 正常优先级：1
     * 低优先级：2
     */
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 抓取方式
     * 普通抓取：0
     * 代理抓取：1
     * 无头渲染：2
     * 有头渲染：3
     */
    @JsonProperty("fetch_method")
    public Integer fetchMethod;

    /**
     * 抓取URL类型
     * 内容页：0
     * 列表页：1
     * 图片视频资源：2
     * 视频流：3
     */
    @JsonProperty("category")
    public Integer category;

    /**
     * 抓取并发级别
     * domain:0
     * host:1
     */
    @JsonProperty("concurrent_level")
    public Integer concurrentLevel;

    /**
     * 全局抽链范围
     * 所有：1
     * 域domain：2
     * 站点host：3
     */
    @JsonProperty("scope")
    public Integer scope;

    /**
     * 抓取URL
     */
    @JsonProperty("url")
    public String url;

    /**
     * HTTP header信息
     */
    @JsonProperty("headers")
    public Map<String, String> headers = new HashMap<>();

    /**
     * 用户自定义字段，用于透传数据
     */
    @JsonProperty("user_defined_map")
    public Map<String, Object> userDefinedMap = new HashMap<>();

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            throw new BadRequestException("URL为空");
        }
        if (!Constants.SUPPORT_CONTENT_CATEGORIES.containsKey(category)) {
            logger.error("not support content category[{}]", category);
            throw new BadRequestException(String.format("不支持的URL类型[%d]", category));
        }
        if (fetchMethod == null) fetchMethod = Constants.FETCH_METHOD_COMMON;
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        if (concurrentLevel == null) concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
        if (!Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("not support fetch method[{}]", fetchMethod);
            throw new BadRequestException(String.format("不支持的抓取方式[%d]", fetchMethod));
        }
        if (!Constants.SUPPORT_CONCURRENT_LEVELS.containsKey(concurrentLevel)) {
            logger.error("not support concurrent level[{}]", concurrentLevel);
            throw new BadRequestException(String.format("不支持的并发级别[%d]", concurrentLevel));
        }
        if (priority > Constants.PRIORITY_LOW || priority < Constants.PRIORITY_HIGH) {
            logger.error("not support priority[{}]", priority);
            throw new BadRequestException(String.format("不支持的优先级[%d]", priority));
        }
    }
}
