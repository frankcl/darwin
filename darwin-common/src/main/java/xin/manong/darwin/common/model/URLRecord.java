package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * URL信息
 *
 * @author frankcl
 * @date 2023-03-06 14:28:13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLRecord implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(URLRecord.class);

    /**
     * 超时时间（毫秒）
     */
    @JSONField(name = "timeout")
    @JsonProperty("timeout")
    public Integer timeout;

    /**
     * 任务ID
     */
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 优先级
     */
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 抓取URL
     */
    @JSONField(name = "url")
    @JsonProperty("url")
    public String url;

    /**
     * 父URL
     */
    @JSONField(name = "parent_url")
    @JsonProperty("parent_url")
    public String parentURL;

    /**
     * 抓取URL类型
     */
    @JSONField(name = "category")
    @JsonProperty("category")
    public URLCategory category;

    /**
     * HTTP header信息
     */
    @JSONField(name = "headers")
    @JsonProperty("headers")
    public Map<String, Object> headers = new HashMap<>();

    /**
     * 用户定义字段，透传到抓取结果
     */
    @JSONField(name = "user_defined_info")
    @JsonProperty("user_defined_info")
    public Map<String, Object> userDefinedInfo = new HashMap<>();

    public URLRecord() {
    }

    public URLRecord(String url) {
        this.url = url;
    }

    /**
     * 检测URLRecord有效性
     * 1. url不能为空
     * 2. category不能为空
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(url)) {
            logger.error("url is empty");
            return false;
        }
        if (category == null) {
            logger.error("url category is null");
            return false;
        }
        if (priority == null) priority = Constants.JOB_PRIORITY_NORMAL;
        return true;
    }
}
