package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 种子URL更新请求
 *
 * @author frankcl
 * @date 2025-04-01 10:52:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeedUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5727200756828988838L;

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
     * 种子URL
     */
    @JsonProperty("url")
    public String url;

    /**
     * 唯一key
     */
    @JsonProperty("key")
    public String key;

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
        if (StringUtils.isEmpty(key)) throw new BadRequestException("种子URL key为空");
        if (StringUtils.isEmpty(url) && category == null && fetchMethod == null && priority == null &&
                concurrentLevel == null && timeout == null && scope == null &&
                headers.isEmpty() && userDefinedMap.isEmpty()) {
            throw new BadRequestException("种子更新信息为空");
        }
        if (category != null && !Constants.SUPPORT_CONTENT_CATEGORIES.containsKey(category)) {
            throw new BadRequestException("不支持的URL类型");
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            throw new BadRequestException("不支持的抓取方式");
        }
        if (concurrentLevel != null && !Constants.SUPPORT_CONCURRENT_LEVELS.containsKey(concurrentLevel)) {
            throw new BadRequestException("不支持的并发级别");
        }
        if (scope != null && !Constants.SUPPORT_LINK_SCOPES.containsKey(scope)) {
            throw new BadRequestException("不支持的抽链类型");
        }
        if (priority != null && (priority > Constants.PRIORITY_LOW || priority < Constants.PRIORITY_HIGH)) {
            throw new BadRequestException("不支持的优先级");
        }
    }
}
