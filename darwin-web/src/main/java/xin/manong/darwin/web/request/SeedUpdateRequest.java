package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.HTTPRequest;
import xin.manong.darwin.common.model.PostMediaType;

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
     * HTTP请求
     */
    @JsonProperty("http_request")
    public HTTPRequest httpRequest;

    /**
     * POST请求媒体类型
     */
    @JsonProperty("post_media_type")
    public PostMediaType postMediaType;

    /**
     * 抽链范围
     * 所有：1
     * 域domain：2
     * 站点host：3
     */
    @JsonProperty("link_scope")
    public Integer linkScope;

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
     * 允许分发
     */
    @JsonProperty("allow_dispatch")
    public Boolean allowDispatch;

    /**
     * URL正规化
     */
    @JsonProperty("normalize")
    public Boolean normalize;

    /**
     * HTTP请求体
     */
    @JsonProperty("request_body")
    public Map<String, String> requestBody = new HashMap<>();

    /**
     * HTTP header信息
     */
    @JsonProperty("headers")
    public Map<String, String> headers = new HashMap<>();

    /**
     * 用户自定义字段，用于透传数据
     */
    @JsonProperty("custom_map")
    public Map<String, Object> customMap = new HashMap<>();

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("种子key为空");
        if (StringUtils.isEmpty(url) && fetchMethod == null && priority == null && postMediaType == null &&
                timeout == null && linkScope == null && allowDispatch == null && httpRequest == null &&
                normalize == null && headers.isEmpty() && customMap.isEmpty() && requestBody.isEmpty()) {
            throw new BadRequestException("种子更新信息为空");
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            throw new BadRequestException("不支持的抓取方式");
        }
        if (linkScope != null && !Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope)) {
            throw new BadRequestException("不支持的抽链类型");
        }
        if (priority != null && (priority > Constants.PRIORITY_LOW || priority < Constants.PRIORITY_HIGH)) {
            throw new BadRequestException("不支持的优先级");
        }
    }
}
